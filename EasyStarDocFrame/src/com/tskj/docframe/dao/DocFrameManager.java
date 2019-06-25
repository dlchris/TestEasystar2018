package com.tskj.docframe.dao;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.istack.internal.NotNull;
import com.tskj.core.db.ColumnInfo;
import com.tskj.core.db.DbUtility;
import com.tskj.docno.dao.DocNoDao;

public class DocFrameManager {

    public String getTableName(String classId, String classType) {
        String sql = "";
        switch (classType) {
            case "0":
                sql = "select doctable as 'tablename' from classtree where classid='" + classId + "'";
                break;
            case "1":
                sql = "select boxtable as 'tablename' from classtree where classid='" + classId + "'";
                break;
            case "2":
                sql = "select rooltable as 'tablename' from classtree where classid='" + classId + "'";
                break;
            default:
                break;
        }
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list == null) {
            return "";
        }
        return list.get(0).get("tablename").toString();
    }

    /**
     * @param
     * @return
     * @author
     * @time $time$
     * @method
     * @version V1.0.0
     * @description
     */
    public List<Map<String, Object>> getDocframeWithSort(String tableName, String perFixDes) throws SQLException {
        return getDocframeWithSort(tableName, perFixDes, "*", "*");
    }

    public List<Map<String, Object>> getDocframeWithSort(String tableName, String perFixDes, @NotNull String fields) throws SQLException {
        return getDocframeWithSort(tableName, perFixDes, fields, "*");
    }

    public List<Map<String, Object>> getDocframeWithSort(String tableName, String perFixDes, @NotNull String fields, @NotNull String fieldName) throws SQLException {
        String sql;
        if (!fields.isEmpty()) {
            sql = "SELECT " + fields + " FROM DOCFRAME WHERE tablename='"
                    + tableName + "'";
        } else {
            sql = "SELECT * FROM DOCFRAME WHERE tablename='"
                    + tableName + "'";
        }
        if (!"*".equals(fieldName)) {
            sql += " AND FIELDNAME='" + fieldName + "'";
        }
        sql += " AND (FIELDSTATE='F' OR FIELDSTATE='U') AND USED='0'";
        sql += " ORDER BY DOCNO,";
        if (!perFixDes.isEmpty()) {
            sql += getDocNoOrderBy(perFixDes) + ",";
        }
        sql += " CASE WHEN FIELDNAME='DOCNO' THEN 0 ELSE 1 END, ORDERBY DESC";
        //System.err.println("字典项值:"+sql);
        return DbUtility.execSQL(sql);
    }

    public List<Map<String, Object>> getDocframe(String tableName) throws SQLException {
        return getDocframe(tableName, "*", "*");
    }

    public List<Map<String, Object>> getDocframe(String tableName, @NotNull String fields) throws SQLException {
        return getDocframe(tableName, fields, "*");
    }

    public List<Map<String, Object>> getDocframe(String tableName, @NotNull String fields, @NotNull String fieldName) throws SQLException {
        String sql;
        if (!fields.isEmpty()) {
            sql = "SELECT " + fields + " FROM DOCFRAME WHERE tablename='"
                    + tableName + "'";
        } else {
            sql = "SELECT * FROM DOCFRAME WHERE tableName='"
                    + tableName + "'";
        }
        if (!"*".equals(fieldName)) {
            sql += " AND (FIELDNAME='" + fieldName + "' OR DISSTR= '" + fieldName + "')";
        }
        sql += " AND (FIELDSTATE='F' OR FIELDSTATE='U') AND USED='0'";
        sql += " ORDER BY ORDERBY DESC";
//        sql += getDocNoOrderBy(classId);
//        sql += " CASE WHEN FIELDNAME='DOCNO' THEN 0 ELSE 1 END, ORDERBY DESC";
//        System.out.println(sql);
        System.err.println("查询对应字段:"+sql);
        return DbUtility.execSQL(sql);
    }

    private String getDocNoOrderBy(String perFixDes) {
        DocNoDao docNoDao = new DocNoDao();
        return docNoDao.getDocNoSort(perFixDes);
    }

    public List<Map<String, Object>> getFieldList(String tableName) throws SQLException {
        // TODO Auto-generated method stub
        String sql = "SELECT * from docframe where tablename='" + tableName + "' AND (FIELDSTATE='F' OR FIELDSTATE='U' OR FIELDNAME='DOCID' OR FIELDNAME='BOXID' OR FIELDNAME='ROOLID') AND USED='0' ORDER BY orderby DESC";
        System.out.println(sql);
        return DbUtility.execSQL(sql);
    }

    public String getFieldListToString(String tableName) {
        if (tableName.isEmpty()) {
            return "";
        }
        List<Map<String, Object>> list = DbUtility.execSQL("SELECT * FROM DOCFRAME WHERE TABLENAME='" + tableName + "' AND (FIELDSTATE='F' OR FIELDSTATE='U') AND USED='0' ORDER BY ORDERBY DESC");
        if (list == null) {
            return "";
        }
        String fieldList = "";
        for (int i = 0; i < list.size(); i++) {
            fieldList += String.format("%s.%s AS %s,", tableName, list.get(i).get("FIELDNAME").toString().trim(), list.get(i).get("DISSTR").toString().trim());
        }
        if (tableName.contains("DOCUMENT")) {
            fieldList += tableName + ".DOCID," + tableName + ".ISDEL";
        } else if (tableName.contains("BOXINFO")) {
            fieldList += tableName + ".BOXID," + tableName + ".ISDEL";
        } else if (tableName.contains("ROOLINFO")) {
            fieldList += tableName + ".ROOLID," + tableName + ".ISDEL";
        }
        return fieldList;

    }

    public List<ColumnInfo> getColumns(String tableName) throws SQLException {
        List<ColumnInfo> result = new ArrayList<ColumnInfo>();
        String sql = "SELECT * FROM " + tableName + " WHERE 0=1";
        ResultSetMetaData data = DbUtility.getResultSetMetaData(sql);
        ColumnInfo col;
        for (int i = 1; i <= data.getColumnCount(); i++) {
            col = new ColumnInfo();
            col.setColumnName(data.getColumnName(i));
            col.setColumnType(data.getColumnType(i));
            col.setSize(data.getPrecision(i));
            result.add(col);
        }
        return result;
    }

    public String getKeyField(String classId, String classType) {
        switch (classType) {
            case "0":
                return "DOCID";
            case "1":
                return "BOXID";
            case "2":
                return "ROOLID";
            default:
                return "";
        }
    }
}
