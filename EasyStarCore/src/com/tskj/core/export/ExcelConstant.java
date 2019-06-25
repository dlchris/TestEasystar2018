package com.tskj.core.export;

/**
 * @author qjwyss
 * @date 2018/9/19
 * @description EXCEL常量类
 */
public class ExcelConstant {

    /**
     * 每个sheet存储的记录数 100W
     */
    public static final Integer PER_SHEET_ROW_COUNT = 1000000;

    /**
     * 每次向EXCEL写入的记录数(查询每页数据大小) 20W
     */
    public static final Integer PER_WRITE_ROW_COUNT = 200000;

    /**
     * 每个sheet的写入次数 5
     */
    public static final Integer PER_SHEET_WRITE_COUNT = PER_SHEET_ROW_COUNT / PER_WRITE_ROW_COUNT;

    /**
     * 每个sheet存储的记录数 6W
     */
    public static final Integer PER_SHEET_ROW_COUNT_2003 = 60000;

    /**
     * 每次向EXCEL写入的记录数(查询每页数据大小) 2W
     */
    public static final Integer PER_WRITE_ROW_COUNT_2003 = 20000;

    /**
     * 每个sheet的写入次数 3
     */
    public static final Integer PER_SHEET_WRITE_COUNT_2003 = PER_SHEET_ROW_COUNT_2003 / PER_WRITE_ROW_COUNT_2003;
}
