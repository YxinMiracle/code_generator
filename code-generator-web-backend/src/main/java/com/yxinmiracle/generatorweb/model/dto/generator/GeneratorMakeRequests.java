package com.yxinmiracle.generatorweb.model.dto.generator;

/*
 * 制作代码生成器请求
 * @author  YxinMiracle
 * @date  2024-06-26 15:10
 * @Gitee: https://gitee.com/yxinmiracle
 */

import com.yxinmiracle.maker.meta.Meta;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class GeneratorMakeRequests implements Serializable {

    private Meta meta;

    private String zipFilePath;

    private static final long serialVersionUID = 1L;

}
