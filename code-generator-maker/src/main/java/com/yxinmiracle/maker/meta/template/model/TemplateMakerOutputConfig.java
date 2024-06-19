package com.yxinmiracle.maker.meta.template.model;

/*
 * @author  YxinMiracle
 * @date  2024-06-19 19:53
 * @Gitee: https://gitee.com/yxinmiracle
 */

import lombok.Data;

@Data
public class TemplateMakerOutputConfig {

    // 从未分组的文件中移除未分组的同名文件
    private boolean removeGroupFilesFromRoot = true;


}
