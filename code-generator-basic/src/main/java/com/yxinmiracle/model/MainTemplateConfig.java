package com.yxinmiracle.model;

/*
 * @author  YxinMiracle
 * @date  2024-06-06 15:32
 * @Gitee: https://gitee.com/yxinmiracle
 * 静态模板类型
 */
import lombok.Data;


@Data
public class MainTemplateConfig {
    // 填充值
    private String author = "user";
    // 输出信息
    private String outputText = "output";
    // 是否循环（开关）
    private boolean loop;
}
