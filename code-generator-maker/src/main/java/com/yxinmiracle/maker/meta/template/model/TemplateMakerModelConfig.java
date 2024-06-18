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
public class TemplateMakerModelConfig {

    private List<ModelInfoConfig> models;

    // 每一次的制作都分成一个组
    private ModelGroupConfig modelGroupConfig;

    @NoArgsConstructor
    @Data
    public static class ModelInfoConfig{
        private String fieldName;
        private String type;
        private String description;
        private Object defaultValue;
        private String abbr;

        // 哪些文本需要进行替换
        private String replaceText;
    }

    @Data
    @NoArgsConstructor
    public static class ModelGroupConfig{
        private String groupKey;
        private String groupName;
        private String condition;
    }

}
