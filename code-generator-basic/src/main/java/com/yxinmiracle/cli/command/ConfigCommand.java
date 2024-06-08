package com.yxinmiracle.cli.command;

import cn.hutool.core.util.ReflectUtil;
import com.yxinmiracle.model.MainTemplateConfig;
import lombok.Data;
import picocli.CommandLine.Command;

import java.lang.reflect.Field;

/*
 * @author  YxinMiracle
 * @date  2024-06-08 14:54
 * @Gitee: https://gitee.com/yxinmiracle
 */
@Command(name = "config", description = "查看参数信息", mixinStandardHelpOptions = true)
@Data
public class ConfigCommand implements Runnable {

    public void run() {
        // 实现 config 命令的逻辑
        System.out.println("查看参数信息");

//        // 获取要打印属性信息的类
//        Class<?> myClass = MainTemplateConfig.class;
//        // 获取类的所有字段
//        Field[] fields = myClass.getDeclaredFields();

        Field[] fields = ReflectUtil.getFields(MainTemplateConfig.class);

        // 遍历并打印每个字段的信息
        for (Field field : fields) {
            System.out.println("字段名称：" + field.getName());
            System.out.println("字段类型：" + field.getType());
            System.out.println("---");
        }
    }
}