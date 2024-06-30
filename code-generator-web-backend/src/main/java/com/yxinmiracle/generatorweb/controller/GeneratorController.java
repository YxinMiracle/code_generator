package com.yxinmiracle.generatorweb.controller;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.utils.IOUtils;
import com.yxinmiracle.generatorweb.annotation.AuthCheck;
import com.yxinmiracle.generatorweb.common.BaseResponse;
import com.yxinmiracle.generatorweb.common.DeleteRequest;
import com.yxinmiracle.generatorweb.common.ErrorCode;
import com.yxinmiracle.generatorweb.common.ResultUtils;
import com.yxinmiracle.generatorweb.constant.UserConstant;
import com.yxinmiracle.generatorweb.exception.BusinessException;
import com.yxinmiracle.generatorweb.exception.ThrowUtils;
import com.yxinmiracle.generatorweb.manager.CacheManager;
import com.yxinmiracle.generatorweb.manager.CosManager;
import com.yxinmiracle.maker.generator.main.GenerateTemplate;
import com.yxinmiracle.maker.generator.main.ZipGenerator;
import com.yxinmiracle.maker.meta.Meta;
import com.yxinmiracle.generatorweb.model.dto.generator.*;
import com.yxinmiracle.generatorweb.model.entity.Generator;
import com.yxinmiracle.generatorweb.model.entity.User;
import com.yxinmiracle.generatorweb.model.vo.GeneratorVO;
import com.yxinmiracle.generatorweb.service.GeneratorService;
import com.yxinmiracle.generatorweb.service.UserService;
import com.yxinmiracle.maker.meta.MetaValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 帖子接口
 */
@RestController
@RequestMapping("/generator")
@Slf4j
public class GeneratorController {

    @Resource
    private GeneratorService generatorService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CacheManager cacheManager;

    // region 增删改查

    /**
     * 创建
     *
     * @param generatorAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addGenerator(@RequestBody GeneratorAddRequest generatorAddRequest, HttpServletRequest request) {
        if (generatorAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorAddRequest, generator);
        List<String> tags = generatorAddRequest.getTags();
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfig fileConfig = generatorAddRequest.getFileConfig();
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfig modelConfig = generatorAddRequest.getModelConfig();
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));

        generatorService.validGenerator(generator, true);
        User loginUser = userService.getLoginUser(request);
        generator.setUserId(loginUser.getId());
        generator.setStatus(0);
        boolean result = generatorService.save(generator);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newGeneratorId = generator.getId();
        return ResultUtils.success(newGeneratorId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteGenerator(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldGenerator.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = generatorService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param generatorUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateGenerator(@RequestBody GeneratorUpdateRequest generatorUpdateRequest) {
        if (generatorUpdateRequest == null || generatorUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorUpdateRequest, generator);
        List<String> tags = generatorUpdateRequest.getTags();
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfig fileConfig = generatorUpdateRequest.getFileConfig();
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfig modelConfig = generatorUpdateRequest.getModelConfig();
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        // 参数校验
        generatorService.validGenerator(generator, false);
        long id = generatorUpdateRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<GeneratorVO> getGeneratorVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(generatorService.getGeneratorVO(generator, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param generatorQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Generator>> listGeneratorByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                 HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    @PostMapping("/list/page/vo/fast")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPageFast(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                     HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();

        // 优先从缓存读取
        String pageCacheKey = getPageCacheKey(generatorQueryRequest);
        String cacheValue = cacheManager.get(pageCacheKey);
        if (StringUtils.isNotBlank(cacheValue)){
            // todo 需要进行了解
            Page<GeneratorVO> generatorVOPage = JSONUtil.toBean(cacheValue, new TypeReference<Page<GeneratorVO>>() {
            }, false);
            return ResultUtils.success(generatorVOPage);
        }

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Generator> queryWrapper = generatorService.getQueryWrapper(generatorQueryRequest);
        queryWrapper.select("id", "name", "description", "tags", "picture", "status", "userId", "createTime", "updateTime");
        Page<Generator> generatorPage = generatorService.page(
                new Page<>(current, size),
                queryWrapper);
        Page<GeneratorVO> result = generatorService.getGeneratorVOPage(generatorPage, request);
        // 写入缓存
        // 缓存一定要设置过期时间
        cacheManager.put(pageCacheKey, JSONUtil.toJsonStr(result));
        return ResultUtils.success(result);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listMyGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                   HttpServletRequest request) {
        if (generatorQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        generatorQueryRequest.setUserId(loginUser.getId());
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param generatorEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editGenerator(@RequestBody GeneratorEditRequest generatorEditRequest, HttpServletRequest request) {
        if (generatorEditRequest == null || generatorEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorEditRequest, generator);
        List<String> tags = generatorEditRequest.getTags();
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfig fileConfig = generatorEditRequest.getFileConfig();
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfig modelConfig = generatorEditRequest.getModelConfig();
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        // 参数校验
        generatorService.validGenerator(generator, false);
        User loginUser = userService.getLoginUser(request);
        long id = generatorEditRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldGenerator.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

    @GetMapping("/download")
    public void downloadGeneratorById(long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        String filePath = generator.getDistPath();
        if (StringUtils.isBlank(filePath)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "产物包不存在");
        }

        // 日志记录
        log.info("用户 {}, 下载了 {}", loginUser, filePath);

        response.setContentType("application/octet-stream;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filePath);

        String zipFilePath = getCacheFilePath(id, filePath);
        if (FileUtil.exist(zipFilePath)) {
            Files.copy(Paths.get(zipFilePath), response.getOutputStream());
            return;
        }

        InputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObj(filePath);
            cosObjectInput = cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);

            // 写入相应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 用完流之后一定要调用 close()
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }

    }

    /**
     * 使用代码生成器
     *
     * @param generatorUseRequests
     * @throws IOException
     */
    @PostMapping("/use")
    public void useGenerator(@RequestBody GeneratorUseRequests generatorUseRequests, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 获取请求参数
        Long id = generatorUseRequests.getId();
        Map<String, Object> dataModel = generatorUseRequests.getDataModel();

        // 需要让用户进行登录，登录了才可以使用
        User loginUser = userService.getLoginUser(request);
        log.info("userId:{}, 使用的生成器的Id:{}", loginUser.getId(), id);
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 从对象存储下载生成器的压缩包
        String distPath = generator.getDistPath();
        if (StringUtils.isBlank(distPath)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "生成器压缩包不存在");
        }


        // 定义一个工作空间（独立的工作空间）
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = String.format("%s/.temp/use/%s", projectPath, id);

        String zipFilePath = tempDirPath + "/dist.zip";

        if (!FileUtil.exist(zipFilePath)) {
            FileUtil.touch(zipFilePath);
        }

        try {
            cosManager.download(distPath, zipFilePath);
        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器下载失败");
        }

        // 压缩包解压，得到脚本文件
        File unzipDistDir = ZipUtil.unzip(zipFilePath);

        // 将用户输入的参数写到Json文件中
        String dataModelFilePath = tempDirPath + "/dataModel.json";
        String jsonStr = JSONUtil.toJsonStr(dataModel);
        FileUtil.writeUtf8String(jsonStr, dataModelFilePath);

        // 执行脚本，得到生成器的文件
        File scriptFile = FileUtil.loopFiles(unzipDistDir, 2, null).stream().filter(file -> {
            if (file.isFile()) {
                return "generator.bat".equals(file.getName());
//                return "generator".equals(file.getName());
            }
            return false;
        }).findFirst().orElseThrow(RuntimeException::new);
        // 添加可执行权限
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
            Files.setPosixFilePermissions(scriptFile.toPath(), permissions);
        } catch (Exception e) {

        }

        File scriptDir = scriptFile.getParentFile();
        // 系统不同
        String scriptAbsolutePath = scriptFile.getAbsolutePath().replace("\\", "/");
        String[] commands = new String[]{scriptAbsolutePath, "json-generate", "--file=" + dataModelFilePath};

//        String scriptAbsolutePath = scriptFile.getAbsolutePath();
//        String[] commands = new String[]{scriptAbsolutePath, "json-generate", "--file=" + dataModelFilePath};

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(scriptDir);

        try {
            Process process = processBuilder.start();

            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("命令执行结束，推出码为：" + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行生成器脚本错误");
        }

        // 压缩得到的生成结果，返回给前端
        String generatedPath = scriptDir.getAbsolutePath() + "/generated";
        String resultPath = tempDirPath + "/result.zip";
        File resultFile = ZipUtil.zip(generatedPath, resultPath);

        response.setContentType("application/octet-stream;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + resultFile.getName());

        Files.copy(resultFile.toPath(), response.getOutputStream());

        // 删除工作空间
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
    }

    /**
     * 制作代码生成器
     *
     * @param generatorMakeRequests
     * @throws IOException
     */
    @PostMapping("/make")
    public void makeGenerator(@RequestBody GeneratorMakeRequests generatorMakeRequests, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 输入参数
        Meta meta = generatorMakeRequests.getMeta();
        String zipFilePath = generatorMakeRequests.getZipFilePath();

        // 需要用户登录
        User loginUser = userService.getLoginUser(request);
        log.info("userId:{}, 在线制作生成器", loginUser.getId());

        // 创建独立的工作空间，下载压缩包到本地
        String projectPath = System.getProperty("user.dir");
        String id = IdUtil.getSnowflakeNextIdStr() + RandomUtil.randomString(6);
        String tempDirPath = String.format("%s/.temp/make/%s", projectPath, id);
        String localZipFilePath = tempDirPath + "/project.zip";
        if (!FileUtil.exist(localZipFilePath)) {
            FileUtil.touch(localZipFilePath);
        }

        // 解压，得到项目的模板文件
        try {
            cosManager.download(zipFilePath, localZipFilePath);
        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩包下载失败");
        }
        // 压缩包解压，得到脚本文件
        File unzipDistDir;
        try {
            unzipDistDir = ZipUtil.unzip(localZipFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "服务器解压压缩包失败");
        }

        // 构造meta对象，构造生成器的输出路径
        String sourceRootPath = unzipDistDir.getAbsolutePath();
        meta.getFileConfig().setSourceRootPath(sourceRootPath);

        MetaValidator.doValidAndFill(meta);
        String outputPath = tempDirPath + "/generated/" + meta.getName();

        // 调用maker的方法制作生成器
        GenerateTemplate generateTemplate = new ZipGenerator();
        try {
            generateTemplate.doGenerate(meta, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "制作失败");
        }

        // 下载制作好的生成器压缩包
        String suffix = "-dist.zip";
        String zipFileName = meta.getName() + suffix;
        // 生成器压缩包的绝对路径
        String distZipFilePath = outputPath + suffix;

        // 清理工作空间
        response.setContentType("application/octet-stream;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + distZipFilePath);

        Files.copy(Paths.get(distZipFilePath), response.getOutputStream());

        // 删除工作空间
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
    }

    @PostMapping("/cache")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void cacheGeneratorById(@RequestBody GeneratorCacheRequests generatorCacheRequests, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (generatorCacheRequests == null || generatorCacheRequests.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long id = generatorCacheRequests.getId();
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        String distPath = generator.getDistPath();
        if (StringUtils.isBlank(distPath)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "产物包不存在");
        }

        String zipFilePath = getCacheFilePath(id, distPath);

        try {
            cosManager.download(distPath, zipFilePath);
        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器下载失败");
        }

        InputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObj(distPath);
            cosObjectInput = cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + distPath);
            // 写入相应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 用完流之后一定要调用 close()
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }

    /**
     * 获取缓存文件路径
     *
     * @param id
     * @param distPath
     * @return
     */
    public String getCacheFilePath(long id, String distPath) {
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = String.format("%s/.temp/cache/%s", projectPath, id);
        String localZipFilePath = tempDirPath + "/" + distPath;
        return localZipFilePath;
    }

    private static String getPageCacheKey(GeneratorQueryRequest generatorQueryRequest){
        String jsonStr = JSONUtil.toJsonStr(generatorQueryRequest);
        String base64 = Base64Encoder.encode(jsonStr);
        String key = "generator:page:" + base64;
        return key;
    }
}
