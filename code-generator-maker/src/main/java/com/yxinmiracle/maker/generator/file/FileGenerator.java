package com.yxinmiracle.maker.generator.file;

/*
 * @author  YxinMiracle
 * @date  2024-06-06 16:48
 * @Gitee: https://gitee.com/yxinmiracle
 */

import com.yxinmiracle.maker.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class FileGenerator {

    public static void doGenerator(DataModel dataModel) throws TemplateException, IOException {
        String projectPath = System.getProperty("user.dir");
        // 静态文件输入路径
        String inputPath = projectPath + File.separator + "code-generator-demo-projects"+ File.separator +"acm-template";
        // 静态文件输出路径
        String outputPath = projectPath;
        StaticFileGenerator.copyFileByHuTool(inputPath, outputPath);
        // todo 这里会出现乱码现象，需要更改
        // 模板路径
        String dynamicInputPath = projectPath + File.separator + "code-generator-maker" + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        // 模板路径添加好内容后应该放在哪
        String dynamicOutputPath = projectPath + File.separator + "acm-template/src/com/yxinmiracle/acm/MainTemplate.java";
        // 模板路径填充的内容
        DynamicFileGenerator.doGenerator(dynamicInputPath, dynamicOutputPath, dataModel);
    }
}
