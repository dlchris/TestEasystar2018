package com.tskj.replace.bean;

/**
 * 成批替换类型
 */
public enum ReplaceType {
    /**
     * 未知
     */
    _NONE(-1),

    /**
     * 前缀
     */
    PREFIX(0),

    /**
     * 后缀
     */
    SUFFIX(1),

    /**
     * 部份替换
     */
    PARTIAL(2),

    /**
     * 全部替换
     */
    ALL(3),

    /**
     * +/-n
     */
    ADD(4),

    /**
     * 字段替换
     */
    FIELDNAME(5);

    private final int code;

    ReplaceType(int code) {
        this.code = code;
    }
}
