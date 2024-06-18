package com.yxinmiracle.maker.meta.template.enums;

/*
 * @author  YxinMiracle
 * @date  2024-06-10 12:21
 * @Gitee: https://gitee.com/yxinmiracle
 * 文件过滤规则
 */

import cn.hutool.core.util.ObjectUtil;

public enum FileFilterRuleEunm {
    CONTENTS("包含", "contains"),
    STARTS_WITH("前缀匹配", "startWith"),
    ENDS_WITH("后缀匹配", "endsWith"),
    REGEX("正则", "regex"),
    EQUALS("相等", "equals");

    private final String text;
    private final String value;


    FileFilterRuleEunm(String text, String value) {
        this.text = text;
        this.value = value;
    }


    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public static FileFilterRuleEunm getEnumByValue(String value){
        if (ObjectUtil.isEmpty(value)){
            return null;
        }
        for (FileFilterRuleEunm anEnum : FileFilterRuleEunm.values()) {
            if (anEnum.value.equals(value)){
                return anEnum;
            }
        }
        return null;
    }

}
