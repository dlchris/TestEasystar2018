package com.tskj.core.db.dao;

import com.tskj.core.db.DbUtility;

import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-25 14:30
 **/
public class DbBasicDaoImpl implements DbBasicDao {
    // 以下分頁方式均只适用于通用版式，特殊情自行写

    @Override
    public List<Map<String, Object>> MySqlQueryPage(String sql, int pageSize, int currentPage) {
        String querySql = sql + " limit " + (currentPage - 1) * pageSize + "," + pageSize + "";
        return DbUtility.execSQL(querySql);
    }

    @Override
    public List<Map<String, Object>> MsSqlQuerypage(String sql, int pageSize, int currentPage) {
        int startSize = 0;
        int endSize = 0;
        if ((currentPage - 1) * pageSize >= 0) {
            startSize = (currentPage - 1) * pageSize + 1;
        }
        endSize += startSize + pageSize - 1;
       /* String orderStr = " ";
        if (!"".equals(toString(sort))) {
            orderStr = orderStr + " order by " + sort;
            if (!"".equals(toString(sortType))) {
                orderStr = orderStr + " " + sortType;
            }
        }*/
        String querySql = " SELECT *  FROM (" + sql + ")  AS tableInfo " +
                "WHERE tableInfo.rowNum BETWEEN '" + startSize + "' AND  '" + endSize + "' " ;//+ orderStr
        System.err.println("querySql:" + querySql);
        return DbUtility.execSQL(querySql);
    }

    @Override
    public List<Map<String, Object>> newMsSqlQuerypage(String sql, int pageSize, int currentPage) {
        String querySql = "SELECT top "+pageSize+" * FROM ("+sql+") AS b " + "WHERE rowNum > "+(currentPage - 1) * pageSize+"";
        return DbUtility.execSQL(querySql);
    }

    @Override
    public int TotalRows(String sql) {
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list != null && !list.isEmpty()) {
            int num = Integer.parseInt(list.get(0).get("num").toString());
            return num;
        }
        return 0;
    }

    public String toString(Object obj) {
        return (obj == null ? "" : obj.toString());

    }
}
