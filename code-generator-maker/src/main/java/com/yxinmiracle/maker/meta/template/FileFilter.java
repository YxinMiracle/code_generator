package com.yxinmiracle.maker.meta.template;

/*
 * @author  YxinMiracle
 * @date  2024-06-17 20:44
 * @Gitee: https://gitee.com/yxinmiracle
 */

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.yxinmiracle.maker.meta.template.enums.FileFilterRangeEnum;
import com.yxinmiracle.maker.meta.template.enums.FileFilterRuleEunm;
import com.yxinmiracle.maker.meta.template.model.FileFilterConfig;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class FileFilter {

    /**
     * 对某个文件或者某个目录进行文件过滤
     * @param filePath
     * @param fileFilterConfigs
     * @return
     */
    public static List<File> doFilter(String filePath, List<FileFilterConfig> fileFilterConfigs){
        List<File> fileList = FileUtil.loopFiles(filePath);
        List<File> collect = fileList.stream().filter(file -> {
            return doSingleFileFilter(fileFilterConfigs, file);
        }).collect(Collectors.toList());
        return collect;
    }

    public static boolean doSingleFileFilter(List<FileFilterConfig> fileFilterConfigs, File file) {
        String fileName = file.getName();
        String fileContent = FileUtil.readUtf8String(file);

        boolean result = true;

        if (CollUtil.isEmpty(fileFilterConfigs)) {
            return true;
        }

        for (FileFilterConfig fileFilterConfig : fileFilterConfigs) {
            String range = fileFilterConfig.getRange();
            String rule = fileFilterConfig.getRule();
            String value = fileFilterConfig.getValue();

            FileFilterRangeEnum fileFilterRangeEnum = FileFilterRangeEnum.getEnumByValue(range);

            if (fileFilterRangeEnum == null) {
                continue;
            }

            String content = fileName;
            switch (fileFilterRangeEnum) {
                case FILE_NAME:
                    content = fileName;
                    break;
                case FILE_CONTENT:
                    content = fileContent;
                    break;
            }

            FileFilterRuleEunm fileFilterRuleEunm = FileFilterRuleEunm.getEnumByValue(rule);
            if (fileFilterRuleEunm == null) {
                continue;
            }

            switch (fileFilterRuleEunm) {
                case CONTENTS:
                    result = content.contains(value);
                    break;
                case STARTS_WITH:
                    result = content.startsWith(value);
                    break;
                case ENDS_WITH:
                    result = content.endsWith(value);
                    break;
                case REGEX:
                    result = content.matches(value);
                    break;
                case EQUALS:
                    result = content.equals(value);
                    break;
                default:
            }

            if (!result) {
                return false;
            }
        }
        return true;
    }

}
