package com.tskj.classtable.search.consts;

/**
 * @author LeonSu
 */

public enum OperationType {
    /**
     * 等于
     */
    EQUAL,

    /**
     * 不等于
     */
    NOT_EQUAL,

    /**
     * 大于
     */
    GREATER,

    /**
     * 小于或等于（不大于）
     */
    NOT_GREATER,

    /**
     * 小于
     */
    LESS,

    /**
     * 大于或等于（不小于）
     */
    NOT_LESS,

    /**
     * 包含
     */
    LIKE,

    /**
     * 不包含
     */
    NOT_LIKE
}
