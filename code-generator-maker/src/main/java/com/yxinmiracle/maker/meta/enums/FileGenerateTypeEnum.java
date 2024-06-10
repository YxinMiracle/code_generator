package com.yxinmiracle.maker.meta.enums;

/*
 * @author  YxinMiracle
 * @date  2024-06-10 12:21
 * @Gitee: https://gitee.com/yxinmiracle
 */

public enum FileGenerateTypeEnum {
    DYNAMIC("动态", "dynamice"),
    STATIC("静态", "static");

    private final String text;
    private final String value;


    FileGenerateTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }


    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
