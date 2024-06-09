package com.yxinmiracle.maker.generator.file;

/*
 * @author  YxinMiracle
 * @date  2024-06-05 19:52
 * @Gitee: https://gitee.com/yxinmiracle
 */

import cn.hutool.core.io.FileUtil;

public class StaticFileGenerator {

    /**
     *
     * @param inputPath 输入路径
     * @param outputPath 输出路径
     */
    public static void copyFileByHuTool(String inputPath, String outputPath){
        FileUtil.copy(inputPath, outputPath, false);
    }

}
