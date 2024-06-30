package com.yxinmiracle.maker.generator;

/*
 * @author  YxinMiracle
 * @date  2024-06-09 13:37
 * @Gitee: https://gitee.com/yxinmiracle
 */

import java.io.*;

public class JarGenerate {
    public static void doGenerate(String projectDir) throws InterruptedException, IOException {
        // todo 修改
        String winMavenCommand = "mvn.cmd clean package -DskipTests=true";
        String otherMavenCommand = "mvn clean package -DskipTests=true";
        String mavenCommand = winMavenCommand;

        ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand.split(" "));
        processBuilder.directory(new File(projectDir));
        Process process = processBuilder.start();

        InputStream inputStream = process.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine())!=null){
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        System.out.println("命令执行结束，推出码为：" + exitCode);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        doGenerate("F:\\code\\code_generator\\code-generator-basic");
    }
}
