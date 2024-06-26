package com.yxinmiracle.generatorweb.model.dto.generator;

/*
 * @author  YxinMiracle
 * @date  2024-06-26 15:10
 * @Gitee: https://gitee.com/yxinmiracle
 */

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class GeneratorUseRequests implements Serializable {

    private Long id;

    // 数据模型
    private Map<String, Object> dataModel;

    private static final long serialVersionUID = 1L;

}
