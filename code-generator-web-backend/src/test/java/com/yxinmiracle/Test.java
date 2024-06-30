package com.yxinmiracle;

/*
 * @author  YxinMiracle
 * @date  2024-06-27 21:34
 * @Gitee: https://gitee.com/yxinmiracle
 */

import cn.hutool.core.util.ZipUtil;

import java.io.File;

public class Test {
    public static void main(String[] args) {
        String localZipFilePath = "F:\\code\\code_generator\\code-generator-demo-projects\\acm-template-pro\\acm-template-pro.rar";
        File unzipDistDir = ZipUtil.unzip(localZipFilePath);
    }
}
