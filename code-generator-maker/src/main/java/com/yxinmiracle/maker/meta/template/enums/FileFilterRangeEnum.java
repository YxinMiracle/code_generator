package com.yxinmiracle.maker.meta.template.enums;

/*
 * @author  YxinMiracle
 * @date  2024-06-17 19:18
 * @Gitee: https://gitee.com/yxinmiracle
 */

import cn.hutool.core.util.ObjectUtil;


public enum FileFilterRangeEnum {
    FILE_NAME("文件名称", "fileName"),
    FILE_CONTENT("文件内容", "fileContent");

    private final String text;
    private final String value;


    FileFilterRangeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }


    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public static FileFilterRangeEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (FileFilterRangeEnum anEnum : FileFilterRangeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
