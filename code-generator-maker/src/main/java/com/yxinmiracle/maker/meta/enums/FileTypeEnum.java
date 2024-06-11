package com.yxinmiracle.maker.meta.enums;

/*
 * @author  YxinMiracle
 * @date  2024-06-10 12:19
 * @Gitee: https://gitee.com/yxinmiracle
 * 文件类型枚举
 */


public enum FileTypeEnum {
    DIR("目录", "dir"),
    GROUP("文件组", "group"),
    File("文件", "file");

    private final String text;
    private final String value;


    FileTypeEnum(String text, String value) {
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
