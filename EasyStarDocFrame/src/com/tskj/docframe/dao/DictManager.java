package com.tskj.docframe.dao;

import com.sun.istack.internal.NotNull;
import com.tskj.core.db.DbUtility;

import java.util.List;
import java.util.Map;

public class DictManager {
    public List<Map<String, Object>> getUserDict(@NotNull String tableName, @NotNull String fieldName) {
        String sql = "SELECT DICTID,DVALUE,FIELDNAME FROM USERDICT WHERE TABLENAME='" + tableName + "'";
        if (!fieldName.isEmpty()) {
            sql += " AND FIELDNAME='" + fieldName + "'";
        }
        sql += " ORDER BY FIELDNAME, DICTID";
        return DbUtility.execSQL(sql);
    }

    public List<Map<String, Object>> getSysDict(@NotNull String fieldName) {
        String sql = "SELECT DICTID,DVALUE,FIELDNAME FROM SYSDICT";
        if (!fieldName.isEmpty()) {
            sql += " WHERE FIELDNAME='" + fieldName + "'";
        }
        sql += " ORDER BY FIELDNAME, DICTID";
        return DbUtility.execSQL(sql);
    }

    public List<Map<String, Object>> getDict(@NotNull String tableName) {
        String sql = "SELECT * FROM (SELECT DICTID,DVALUE,FIELDNAME FROM USERDICT WHERE TABLENAME='" + tableName + "' UNION ALL SELECT DICTID,DVALUE,FIELDNAME FROM SYSDICT where exists(select 1 from docframe where docframe.dicttable='sysdict' and docframe.fieldname=sysdict.fieldname and docframe.tablename='" + tableName + "')) A ";
        sql += " ORDER BY FIELDNAME,DICTID";
        //System.err.println(sql);
        return DbUtility.execSQL(sql);
    }
}
