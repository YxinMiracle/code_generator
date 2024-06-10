package com.yxinmiracle.maker;

/*
 * @author  YxinMiracle
 * @date  2024-06-05 18:45
 * @Gitee: https://gitee.com/yxinmiracle
 */

//import com.yxinmiracle.maker.cli.CommandExecutor;


import com.yxinmiracle.maker.generator.main.MainGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
        MainGenerator mg = new MainGenerator();
        mg.doGenerate();
    }
}