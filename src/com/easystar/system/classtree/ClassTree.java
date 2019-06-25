package com.easystar.system.classtree;

import com.tskj.core.db.DbConnection;
import com.tskj.core.db.DbUtility;
import com.tskj.core.system.consts.DBConsts;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ClassTree {

    public ClassTree() {

    }

    private List<Map<String, Object>> classTree;

    public List<Map<String, Object>> getClassTree(String userID, String roleID) {
//        String sql;
//        try {
//            try (Connection conn = DbConnection.getConnection()) {
//                switch (DbUtility.getDBType()) {
//                    case DBConsts.DB_SQLSERVER:
//                    case DBConsts.DB_MYSQL:
//                        sql = "SELECT DESCRIPTION, CLASSID, dbo.GetClassType(DocTable, BoxTable, RoolTable) AS 'CLASSTYPE' FROM CLASSTREE WHERE CLASSLEVEL='7' AND CLASSTYPE=1 ORDER BY DESCRIPTION";
//                        break;
//                    case DBConsts.DB_ORACLE:
//                        sql = "SELECT 1";
//                        break;
//                    default:
//                        return null;
//                }
//                classTree = DbUtility.execSQL(conn, sql, null);
//                return classTree;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
            return null;
//        }
    }

    /**
     * 根据门类ID和门类类型得到实体表名
     *
     * @param classId
     * @param classType 0：文件级；1：盒级；2：案卷级
     * @return 字符串，如果未找到，返回空字符串
     */
    public String getTableName(String classId, String classType) {
        com.tskj.classtree.dao.ClassTree classTree = new com.tskj.classtree.dao.ClassTree();
        return classTree.getTableName(classId, classType);
    }

    /**
     * @description 根据用户ID，角色ID，门类ID，查出此门类下的所有子门类列表
     */
    public List<Map<String, Object>> getClassTree(String userID, String roleID, String classID) {
        String sql = "";
        switch (DbUtility.getDBType()) {
            case DBConsts.DB_SQLSERVER:
                sql = "WITH locs(classid,parenteclassid) " +
                        "AS " +
                        "( " +
                        "SELECT classid,parenteclassid FROM classtree WHERE classid='" + classID + "' " +
                        "UNION ALL " +
                        "SELECT A.classid,A.parenteclassid FROM classtree A inner join locs B ON A.parenteclassid = B.classid " +
//                "-- b.pid=a.id 向上递归\n" +
                        ") " +
                        "SELECT * FROM classtree WHERE EXISTS(SELECT 1 FROM locs WHERE locs.classid=classtree.classid)";
                break;
            case DBConsts.DB_MYSQL:
                sql = "SELECT 1";
                break;
            case DBConsts.DB_ORACLE:
                sql = "SELECT 1";
                break;
            default:
                break;
        }
        return DbUtility.execSQL(sql);
    }

    public String createClassInfo() {
        return "";
    }

    public String deleteClassInfo() {
        return "";
    }

    public String updateClassInfo() {
        return "";
    }
}
