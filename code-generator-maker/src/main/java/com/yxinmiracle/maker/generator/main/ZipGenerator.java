package com.yxinmiracle.maker.generator.main;

/*
 * @author  YxinMiracle
 * @date  2024-06-22 21:36
 * @Gitee: https://gitee.com/yxinmiracle
 */

public class ZipGenerator extends GenerateTemplate {

    @Override
    protected String buildDist(String outputPath, String sourceCopyDestPath, String jarPath, String shellOutputFilePath) {
        String distPath = super.buildDist(outputPath, sourceCopyDestPath, jarPath, shellOutputFilePath);
        return super.buildZip(distPath);
    }
}
