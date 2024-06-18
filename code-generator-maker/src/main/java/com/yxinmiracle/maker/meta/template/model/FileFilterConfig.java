package com.yxinmiracle.maker.meta.template.model;

/*
 * @author  YxinMiracle
 * @date  2024-06-17 19:02
 * @Gitee: https://gitee.com/yxinmiracle
 */

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileFilterConfig {

    private String range; // 过滤的范围
    private String rule; // 过滤的规则
    private String value; // 过滤的值

}
