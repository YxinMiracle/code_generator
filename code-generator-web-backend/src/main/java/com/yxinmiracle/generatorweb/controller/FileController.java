package com.yxinmiracle.generatorweb.controller;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.utils.IOUtils;
import com.yxinmiracle.generatorweb.annotation.AuthCheck;
import com.yxinmiracle.generatorweb.common.BaseResponse;
import com.yxinmiracle.generatorweb.common.ErrorCode;
import com.yxinmiracle.generatorweb.common.ResultUtils;
import com.yxinmiracle.generatorweb.constant.FileConstant;
import com.yxinmiracle.generatorweb.constant.UserConstant;
import com.yxinmiracle.generatorweb.exception.BusinessException;
import com.yxinmiracle.generatorweb.manager.CosManager;
import com.yxinmiracle.generatorweb.model.dto.file.UploadFileRequest;
import com.yxinmiracle.generatorweb.model.entity.User;
import com.yxinmiracle.generatorweb.model.enums.FileUploadBizEnum;
import com.yxinmiracle.generatorweb.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * 文件接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;


    /**
     * 测试文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 文件上传
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", fileName);
        File file = null;
        try {
            // 上传文件，创建一个零食文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete(); //一定要删除
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    @GetMapping("/test/download")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void testDownloadFile(String filePath, HttpServletResponse response) throws IOException {
        InputStream cosObjectInput = null;

        try {
            COSObject cosObject = cosManager.getObj(filePath);
            cosObjectInput = cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filePath);
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
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
