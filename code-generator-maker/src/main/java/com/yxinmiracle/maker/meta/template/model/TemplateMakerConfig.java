package com.yxinmiracle.maker.meta.template.model;

/*
 * @author  YxinMiracle
 * @date  2024-06-18 21:48
 * @Gitee: https://gitee.com/yxinmiracle
 */

import com.yxinmiracle.maker.meta.Meta;
import lombok.Data;

@Data
public class TemplateMakerConfig {

    private Long id;

    private Meta meta = new Meta();

    private String originProjectPath;

    private TemplateMakerFileConfig fileConfig = new TemplateMakerFileConfig();

    private TemplateMakerModelConfig modelConfig = new TemplateMakerModelConfig();

    private TemplateMakerOutputConfig outputConfig = new TemplateMakerOutputConfig();
}
