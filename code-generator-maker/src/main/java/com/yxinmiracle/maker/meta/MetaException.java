package com.yxinmiracle.maker.meta;

/*
 * @author  YxinMiracle
 * @date  2024-06-10 12:04
 * @Gitee: https://gitee.com/yxinmiracle
 */

public class MetaException extends RuntimeException {

    public MetaException(String message) {
        super(message);
    }

    public MetaException(String message, Throwable cause) {
        super(message, cause);
    }
}