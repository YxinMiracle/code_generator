package com.yxinmiracle.cli.command;

/*
 * @author  YxinMiracle
 * @date  2024-06-08 14:54
 * @Gitee: https://gitee.com/yxinmiracle
 */

import cn.hutool.core.io.FileUtil;
import lombok.Data;
import picocli.CommandLine.Command;

import java.io.File;
import java.util.List;

@Command(name = "list", mixinStandardHelpOptions = true)
@Data
public class ListCommand implements Runnable {

    @Override
    public void run() {
        String projectPath = System.getProperty("user.dir");
        File parentFile = new File(projectPath).getParentFile();
        String inputPath = new File(parentFile, "code-generator-demo-projects" + File.separator + "acm-template").getAbsolutePath();
        List<File> files = FileUtil.loopFiles(inputPath); // 获取这个目录下的所有文件，在这里的目的是能够获取所有的生成文件的文件列表
        for (File file : files) {
            System.out.println(file);
        }

    }
}
