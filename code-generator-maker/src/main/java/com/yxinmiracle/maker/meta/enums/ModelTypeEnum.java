package com.yxinmiracle.maker.meta.enums;

/*
 * @author  YxinMiracle
 * @date  2024-06-10 12:23
 * @Gitee: https://gitee.com/yxinmiracle
 */

public enum ModelTypeEnum {
    STRING("字符串", "String"),
    BOOLEAN("布尔", "boolean");

    private final String text;
    private final String value;


    ModelTypeEnum(String text, String value) {
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
