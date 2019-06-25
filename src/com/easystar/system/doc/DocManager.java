package com.easystar.system.doc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.tskj.core.db.ColumnInfo;
import com.tskj.core.db.DbConnection;
import com.tskj.core.db.DbUtility;
import com.tskj.docframe.dao.DocFrameManager;

public class DocManager {

    public DocManager() {
        docFrameManager = new DocFrameManager();
    }

    private DocFrameManager docFrameManager;

    private String getFieldList(String tableName) {
        String fieldList = docFrameManager.getFieldListToString(tableName);
        return fieldList;
    }

    /**
     * 获取文件级数据表
     */
    public List<Map<String, Object>> getDocTable(String docTableName) throws SQLException {
        String fieldList = getFieldList(docTableName);
        String sql = "SELECT " + fieldList + " FROM " + docTableName + " ORDER BY docno";
        return DbUtility.execSQL(sql);
    }

    /**
     * 获取盒级数据表
     */
    public List<Map<String, Object>> getBoxTable(String boxTableName) throws SQLException {
        String fieldList = getFieldList(boxTableName);
        String sql = "SELECT " + fieldList + " FROM " + boxTableName;
        return DbUtility.execSQL(sql);
    }

    /**
     * 获取案卷级数据表
     */
    public List<Map<String, Object>> getRoolTable(String roolTableName) throws SQLException {
        String fieldList = getFieldList(roolTableName);
        String sql = "SELECT " + fieldList + " FROM " + roolTableName;
        return DbUtility.execSQL(sql);
    }

    public List<Map<String, Object>> getDocInBox(String docTable, String docNo) throws SQLException {
        String fieldList = getFieldList(docTable);
        String sql = "SELECT " + fieldList + " FROM " + docTable + " WHERE docno like '" + docNo + "%' ORDER BY docno";
        return DbUtility.execSQL(sql);
    }

    public int newDocument(String tableName, Map<String, Object> value) throws SQLException {
        List<ColumnInfo> colList = docFrameManager.getColumns(tableName);
        String fieldList = "";
        String paramList = "";
        for (int i = 0; i < colList.size(); i++) {
            fieldList += colList.get(i).getColumnName() + ",";
            paramList += "?,";
        }
        fieldList = fieldList.substring(0, fieldList.length() - 1);
        paramList = paramList.substring(0, paramList.length() - 1);
        String sql = "INSERT INTO " + tableName + "(" + fieldList
                + ") VALUES (" + paramList + ")";
        return 0;//DBUtility.execSQLWithTrans(sql, paramList, value);
    }

    public int editDocument(String tableName, Map<String, Object> value) throws SQLException {
        List<ColumnInfo> colList = docFrameManager.getColumns(tableName);
        String fieldList = "";
        for (int i = 0; i < colList.size(); i++) {
//			System.out.print(colList.get(i).getColumnName() + "=");
//			System.out.println(colList.get(i).getColumnType());
            if (!"DOCID".equals(colList.get(i).getColumnName()) &&
                    !"BOXID".equals(colList.get(i).getColumnName()) &&
                    !"ROOLID".equals(colList.get(i).getColumnName())) {
                if (!value.containsKey(colList.get(i).getColumnName())) {
                    switch (colList.get(i).getColumnType()) {
                        case 4:
                            fieldList += colList.get(i).getColumnName() + "=0,";
                            break;
                        default:
                            fieldList += colList.get(i).getColumnName() + "='',";
                            break;
                    }
                } else {
                    switch (colList.get(i).getColumnType()) {
                        case 4:
                            fieldList += colList.get(i).getColumnName() + "="
                                    + value.get(colList.get(i).getColumnName()) + ",";
                            break;
                        default:
                            fieldList += colList.get(i).getColumnName() + "='"
                                    + value.get(colList.get(i).getColumnName()) + "',";
                            break;
                    }
                }
            }
        }
        fieldList = fieldList.substring(0, fieldList.length() - 1);
        String sql = "UPDATE " + tableName + " SET " + fieldList
                + " WHERE ";
        if (tableName.contains("DOCUMENT")) {
            sql += "DOCID='" + value.get("DOCID") + "'";
        } else if (tableName.contains("BOXINFO")) {
            sql += "BOXID='" + value.get("BOXID") + "'";
        } else if (tableName.contains("ROOLINFO")) {
            sql += "ROOLID='" + value.get("ROOLID") + "'";
        }
        return DbUtility.execSQLWithTrans(sql);
    }
}
