package com.yxinmiracle.maker.meta;

/*
 * @author  YxinMiracle
 * @date  2024-06-09 10:23
 * @Gitee: https://gitee.com/yxinmiracle
 * 业务层的工具类就叫做Manger
 */

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;

public class MetaManger {

    private static volatile Meta meta;

    public static Meta getMetaObj() {
        // a, b, c 同时来了，同时判断都为空，就会同时进行创建了
        if (meta == null) {
            synchronized (MetaManger.class) {
                if (meta == null) {
                    meta = initMeta();
                }
            }
        }
        return meta;
    }

    private static Meta initMeta() {
        String metaJson = ResourceUtil.readUtf8Str("meta.json");
        Meta newMate = JSONUtil.toBean(metaJson, Meta.class);
        // todo 校验配置文件的参数是否合法
        return newMate;
    }

}
