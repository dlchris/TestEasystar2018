package com.tskj.core.db.dao;

import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-25 14:30
 **/
public interface DbBasicDao {
    // 以下分頁方式均只适用于通用版式，特殊情自行写
    /**
     * @Author JRX
     * @Description: mysql 通行式分页
     * @create 2019/4/25 14:33
     * @param sql
     * @param pageSize
     * @param currentPage
     * @return
    **/
    List<Map<String, Object>> MySqlQueryPage(String sql, int pageSize, int currentPage);
    /**
     * @Author JRX
     * @Description: sqlServer 通行式分页
     * 特殊分页方式  此方法分页必须 创建row_number() over(order by 字段 DESC) AS rowNum
     * 创建字段必须为 rowNum 创建row_number() over(order by 字段 DESC) AS rowNum
     * @create 2019/4/25 14:34
     * @param sql
     * @param pageSize
     * @param currentPage
     * @return
    **/
    List<Map<String, Object>> MsSqlQuerypage(String sql, int pageSize, int currentPage);//, String sort, String sortType 排序在外面的sql

    //mssql另一种分页 排序在传入的sql同上
    List<Map<String, Object>> newMsSqlQuerypage(String sql, int pageSize, int currentPage);

    /**
     * 特殊总条数
     * 总条数字段必须去num
     */
    int TotalRows(String sql);

}
