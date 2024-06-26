package com.yxinmiracle.maker.meta.template;

/*
 * @author  YxinMiracle
 * @date  2024-06-16 10:09
 * @Gitee: https://gitee.com/yxinmiracle
 * 这是模板制作工具，制作模板文件
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yxinmiracle.maker.meta.Meta;
import com.yxinmiracle.maker.meta.enums.FileGenerateTypeEnum;
import com.yxinmiracle.maker.meta.enums.FileTypeEnum;
import com.yxinmiracle.maker.meta.template.enums.FileFilterRangeEnum;
import com.yxinmiracle.maker.meta.template.enums.FileFilterRuleEunm;
import com.yxinmiracle.maker.meta.template.model.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateMaker {

    public static long makeTemplate(TemplateMakerConfig templateMakerConfig) {
        Long id = templateMakerConfig.getId();
        Meta meta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig templateMakerFileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig templateMakerModelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig outputConfig = templateMakerConfig.getOutputConfig();
        return makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, outputConfig, id);
    }

    // 所有的文件路径以及对应的配置都是在这里面的
    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerOutputConfig templateMakerOutputConfig, Long id) {

        // 查看id在不在，要是不在就生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = projectPath + File.separator + ".temp";
        // ===== 首先我会用去复制整个项目的所有文件到.temp目录下，然后我们会去检查，看看那个文件加下需要去加ftl模板信息。
        if (!FileUtil.exist(new File(tempDirPath))) {
            // 判断一下目录是否存在, 如果不存在就进行更新
            FileUtil.mkdir(tempDirPath);
        }

        // 工作空间的项目路径
        String templatePath = tempDirPath + File.separator + id;
        if (!FileUtil.exist(new File(templatePath))) {
            // 如果说这个文件夹不存在，就创建
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
        }

        // 2. 输入文件信息
        String sourceRootPath = FileUtil.loopFiles(new File(templatePath), 1, null)
                .stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElseThrow(RuntimeException::new).getAbsolutePath();
        // win系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");
        // 2.1 制作文件模板
        List<Meta.FileConfig.FileInfo> newFileInfoList = makeFileTemplates(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);
        // 2.2 获取模型的配置信息
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = getModelInfoList(templateMakerModelConfig);

        // 三、生成配置文件
        String metaOutputPath = templatePath + File.separator + "meta.json";
        // 如果已经有了，meta文件，那么久进行追加
        if (FileUtil.exist(metaOutputPath)) {
            newMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            // 1。 追加配置参数
            List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);

            // 2. 最佳模型参数
            List<Meta.ModelConfig.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);

            // 进行去重
            newMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            newMeta.getModelConfig().setModels(distinctModels(modelInfoList));
        } else {
            // 构建文件配置
            Meta.FileConfig fileConfig = new Meta.FileConfig();

            fileConfig.setSourceRootPath(sourceRootPath);
            // 添加文件配置
            newMeta.setFileConfig(fileConfig);

            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();
            fileInfoList.addAll(newFileInfoList);
            fileConfig.setFiles(fileInfoList);

            // 设置模型的信息
            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            newMeta.setModelConfig(modelConfig);

            // 添加模型信息，对于模型耳朵信息来说，是需要用户输入的，我们会根据用户输入的文件信息进行挖坑
            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.addAll(newModelInfoList);
        }

        // 使用额外的输出配置
        if (templateMakerOutputConfig != null) {
            // 将出现在分组内的外层文件进行去重
            if (templateMakerOutputConfig.isRemoveGroupFilesFromRoot()) {
                List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
                newMeta.getFileConfig().setFiles(TemplateMakerUtils.removeGroupFilesFromRoot(fileInfoList));
            }
        }

        // 2. 输出原信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        return id;
    }

    private static List<Meta.ModelConfig.ModelInfo> getModelInfoList(TemplateMakerModelConfig templateMakerModelConfig) {
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();

        if (templateMakerModelConfig == null) {
            return newModelInfoList;
        }

        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();

        if (CollUtil.isEmpty(models)) {
            return newModelInfoList;
        }

        // 转换成为配置文件接受的modelInfo对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream().map(modelInfoConfig -> {
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelInfoConfig, modelInfo);
            return modelInfo;
        }).collect(Collectors.toList());

        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();

            BeanUtil.copyProperties(modelGroupConfig, groupModelInfo);

            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }


    private static List<Meta.FileConfig.FileInfo> makeFileTemplates(TemplateMakerFileConfig templateMakerFileConfig,
                                                                    TemplateMakerModelConfig templateMakerModelConfig,
                                                                    String sourceRootPath) {
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();
        if (templateMakerFileConfig == null) {
            return newFileInfoList;
        }

        List<TemplateMakerFileConfig.FileInfoConfig> fileConfigFiles = templateMakerFileConfig.getFiles();
        if (CollUtil.isEmpty(fileConfigFiles)) {
            return newFileInfoList;
        }

        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileConfigFiles) {
            String inputFilePath = fileInfoConfig.getPath();
            String inputFileAbsolutePath = sourceRootPath + File.separator + inputFilePath;
            List<File> fileList = FileFilter.doFilter(inputFileAbsolutePath, fileInfoConfig.getFilterConfigList());

            fileList = fileList.stream().filter(file -> !file.getAbsolutePath().endsWith(".ftl")).collect(Collectors.toList());

            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceRootPath, file, fileInfoConfig);
                newFileInfoList.add(fileInfo);
            }
        }

        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();
            String condition = fileGroupConfig.getCondition();

            Meta.FileConfig.FileInfo groupFileInfo = new Meta.FileConfig.FileInfo();
            groupFileInfo.setCondition(condition);
            groupFileInfo.setGroupKey(groupKey);
            groupFileInfo.setGroupName(groupName);
            groupFileInfo.setFiles(newFileInfoList);
            groupFileInfo.setType(FileTypeEnum.GROUP.getValue());
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(groupFileInfo);
        }
        return newFileInfoList;
    }

    private static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig,
                                                             String sourceRootPath,
                                                             File inputFile,
                                                             TemplateMakerFileConfig.FileInfoConfig fileInfoConfig) {
        String tempFilePath = inputFile.getAbsolutePath();
        tempFilePath = tempFilePath.replaceAll("\\\\", "/");
        // 这是需要挖坑文件的象对路径，用来保存到meta文件里面的
        String fileInputPath = tempFilePath.replace(sourceRootPath + "/", "");
        // 要挖坑的文件信息应该保存成什么格式的文件，这也是象对路径
        String fileOutPutPath = fileInputPath + ".ftl";


        // 二、 使用字符串替换，生成模板文件
        String fileInputAbsolutePath = inputFile.getAbsolutePath();

        String fileOutputAbsolutePath = inputFile.getAbsolutePath() + ".ftl";


        String fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);

        // 是否有模板文件
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);

        // 如果在文件的目录下已经存在了这样的一个模板文件路径，那么就不创建这个文件了，就在这个模板文件的基础上进行挖坑
        if (hasTemplateFile) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        }

        // 支持多个模型，对同一个文件的内容，遍历模型进行多轮替换
        // 这就是需要替换掉的多个模型
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();

        // 这个是获取到最后的模板文件内容，要去写到ft模板文件中
        String newFileContent = fileContent;

        // 这里是这个组的名字
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : models) {
            String fieldName = modelInfoConfig.getFieldName();
            String replacement;
            // 先判断这个模型
            // todo 这个逻辑写的时候也忘记
            if (modelGroupConfig == null) {
                // 置换物。modelInfo是用户指定的需要进行替换的内容
                replacement = String.format("${%s}", fieldName);
            } else {
                String groupKey = modelGroupConfig.getGroupKey();
                // 说明这是有组的情况，要是有组的情况是要加
                replacement = String.format("${%s.%s}", groupKey, fieldName);
            }
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }


        // 添加模板文件和对应的文件信息，是静态的还是动态的
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileOutPutPath);
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setCondition(fileInfoConfig.getCondition());
        fileInfo.setType(FileTypeEnum.File.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());// 默认设置为动态

        // 是否更改了文件内容
        boolean contentEquals = newFileContent.equals(fileContent);
        // 之前不存在模板文件，而且本次的替换并没有修改文件内容，所以就是静态

        // 判断两个文件的内容是否相同，要是说完全相同的话，就是静态文件，要是不是完全相同的话，那就是动态
        if (!hasTemplateFile) {
            if (contentEquals) {
                fileInfo.setInputPath(fileInputPath);
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            }
        } else if (!contentEquals) {
            // 需要更新
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
        }
        return fileInfo;
    }

    // todo 这段代码需要多读读
    private static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {
        // todo 分组操作，可以学习
        Map<String, List<Meta.FileConfig.FileInfo>> groupKeyFileInfoListMap = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.FileConfig.FileInfo::getGroupKey)
                );
        // 同内的配置合并
        Map<String, Meta.FileConfig.FileInfo> groupKeyMergedFileInfoMap = new HashMap<>();

        for (Map.Entry<String, List<Meta.FileConfig.FileInfo>> entry : groupKeyFileInfoListMap.entrySet()) {
            List<Meta.FileConfig.FileInfo> tempFileInfoList = entry.getValue();
            // todo 打平操作，可以学习
            List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream()
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                    ).values());

            Meta.FileConfig.FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            newFileInfo.setFiles(newFileInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedFileInfoMap.put(groupKey, newFileInfo);
        }

        ArrayList<Meta.FileConfig.FileInfo> resultList = new ArrayList<>(groupKeyMergedFileInfoMap.values());

        resultList.addAll(new ArrayList<>(
                fileInfoList.stream()
                        .filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                        .collect(
                                Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                        ).values()));
        return resultList;
    }

    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        // todo 分组操作，可以学习
        Map<String, List<Meta.ModelConfig.ModelInfo>> groupKeyModelInfoListMap = modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey)
                );
        // 同内的配置合并
        Map<String, Meta.ModelConfig.ModelInfo> groupKeyMergedModelInfoMap = new HashMap<>();

        for (Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry : groupKeyModelInfoListMap.entrySet()) {
            List<Meta.ModelConfig.ModelInfo> tempModelInfoList = entry.getValue();
            // todo 打平操作，可以学习
            List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream()
                    .flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                    ).values());

            Meta.ModelConfig.ModelInfo newModelInfo = CollUtil.getLast(tempModelInfoList);
            newModelInfo.setModels(newModelInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedModelInfoMap.put(groupKey, newModelInfo);
        }

        ArrayList<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>(groupKeyMergedModelInfoMap.values());

        resultList.addAll(new ArrayList<>(
                modelInfoList.stream()
                        .filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                        .collect(
                                Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                        ).values()));
        return resultList;
    }


}
