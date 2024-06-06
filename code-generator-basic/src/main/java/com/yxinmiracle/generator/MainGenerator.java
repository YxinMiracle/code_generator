package com.yxinmiracle.generator;

/*
 * @author  YxinMiracle
 * @date  2024-06-06 16:48
 * @Gitee: https://gitee.com/yxinmiracle
 */

import com.yxinmiracle.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException {

        String projectPath = System.getProperty("user.dir");
        // 静态文件输入路径
        String inputPath = projectPath + File.separator + "code-generator-demo-projects"+ File.separator +"acm-template";
        // 静态文件输出路径
        String outputPath = projectPath;
        StaticGenerator.copyFileByHuTool(inputPath, outputPath);
        System.out.println("feat-a branch test");

        // 模板路径
        String dynamicInputPath = projectPath + File.separator + "code-generator-basic" + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        // 模板路径添加好内容后应该放在哪
        String dynamicOutputPath = projectPath + File.separator + "acm-template/src/com/yxinmiracle/acm/MainTemplate.java";
        // 模板路径填充的内容
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("哈哈哈哈哈哈哈");
        mainTemplateConfig.setLoop(false);
        mainTemplateConfig.setOutputText("求和结果：");
        DynamicGenerator.doGenerator(dynamicInputPath, dynamicOutputPath, mainTemplateConfig);
    }
}
