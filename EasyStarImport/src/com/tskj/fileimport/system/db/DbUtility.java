package com.tskj.fileimport.system.db;

import com.alibaba.fastjson.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DbUtility extends com.tskj.core.db.DbUtility {

    /**
     * 执行Update、Delete等SQL语句，带参数，使用事务 执行成功，返回0，否则返回1
     */
    public static JSONObject execSQL(Connection conn, String sql,
                                     List<String> params, List<Map<String, Object>> values) {
        JSONObject ret = JSONObject.parseObject("{}");
        if (com.tskj.core.db.DbUtility.execSQLWithTrans(conn, sql, params, values) == 0) {
            ret.put("result", 0);
        } else {
            ret.put("result", 1);
            ret.put("errmsg", "SQL执行失败");
        }
        return ret;
    }
}
