package com.yxinmiracle.maker.generator.main;

/*
 * @author  YxinMiracle
 * @date  2024-06-09 10:36
 * @Gitee: https://gitee.com/yxinmiracle
 * 这需要读取各种各样的配置文件来进行测试
 */


import freemarker.template.TemplateException;

import java.io.IOException;

public class MainGenerator extends GenerateTemplate {

    @Override
    protected String buildDist(String outputPath, String sourceCopyDestPath, String jarPath, String shellOutputFilePath) {
        System.out.println("不用精简版了");
        return "";
    }

}
