package com.yxinmiracle.maker.generator.file;

/*
 * @author  YxinMiracle
 * @date  2024-06-06 15:39
 * @Gitee: https://gitee.com/yxinmiracle
 */

import cn.hutool.core.io.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class DynamicFileGenerator {

    /**
     * 用于生成模板文件
     * @param inputPath 输入路径
     * @param outputPath 输出路径
     * @param model 对应结构
     * @throws IOException 抛出异常
     * @throws TemplateException 抛出异常
     */
    public static void doGenerator(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setDefaultEncoding("utf-8");
        configuration.setNumberFormat("0.######");

        // 设置模板父路径
        File templateDir = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(templateDir);

        // 设置文件名
        String templateName = new File(inputPath).getName();
        Template template = configuration.getTemplate(templateName);

        // 如果文件不存在，就要创建对应的目录
        if (!FileUtil.exist(outputPath)){
            FileUtil.touch(outputPath);
        }

        Writer out = new FileWriter(outputPath);
        template.process(model, out);
        // 生成文件后别忘了关闭哦
        out.close();
    }
 }
