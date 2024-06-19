package com.yxinmiracle.maker.meta.template.model;

/*
 * @author  YxinMiracle
 * @date  2024-06-17 19:08
 * @Gitee: https://gitee.com/yxinmiracle
 */

import com.yxinmiracle.maker.meta.Meta;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class TemplateMakerFileConfig {

    private List<FileInfoConfig> files;

    // 每一次的制作都分成一个组
    private FileGroupConfig fileGroupConfig;

    @NoArgsConstructor
    @Data
    public static class FileInfoConfig{
        private String path;
        private String condition; // 控制单个文件是否生成
        private List<FileFilterConfig> filterConfigList;
    }

    @Data
    @NoArgsConstructor
    public static class FileGroupConfig{
        private String groupKey;
        private String groupName;
        private String condition;
    }

}
