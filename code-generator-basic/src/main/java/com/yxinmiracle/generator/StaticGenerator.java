package com.yxinmiracle.generator;

/*
 * @author  YxinMiracle
 * @date  2024-06-05 19:52
 * @Gitee: https://gitee.com/yxinmiracle
 */

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class StaticGenerator {
    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");
        // 输入路径
        String inputPath = projectPath + File.separator + "code-generator-demo-projects"+ File.separator +"acm-template";
        // 输出路径
        String outputPath = projectPath;
//        copyFileByRecursive(inputPath, outputPath);
        copyFileByHuTool(inputPath, outputPath);
    }

    /**
     *
     * @param inputPath 输入路径
     * @param outputPath 输出路径
     */
    public static void copyFileByHuTool(String inputPath, String outputPath){
        FileUtil.copy(inputPath, outputPath, false);
    }

    public static void copyFileByRecursive(String inputPath, String outPutPath){
        File inputFile = new File(inputPath);
        File outputFile = new File(outPutPath);
        try {
            copyFileByRecursive(inputFile, outputFile);
        }catch (Exception e){
            System.out.println("复制文件失败");
            e.printStackTrace();
        }
    }

    private static void copyFileByRecursive(File inputFile, File outputFile) throws IOException {
        // 查看是否是目录
        if (inputFile.isDirectory()){
            File destOutPutFile = new File(outputFile, inputFile.getName());
            if (!destOutPutFile.exists()){
                // 如果目录不存在
                destOutPutFile.mkdirs();
            }
            // 获取目录下的所有文件和子目录
            File[] files = inputFile.listFiles();
            // 无子文件。直接结束
            if (ArrayUtil.isEmpty(files)){
                return;
            }
            for (File file : files){
                // 递归拷贝下一层文件
                copyFileByRecursive(file, destOutPutFile);
            }
        }else {
            Path destPath = outputFile.toPath().resolve(inputFile.getPath());
            Files.copy(inputFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
