package com.tskj.core.db;

import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 执行SQL的静态工具类
 * public方法包括：
 * execSQL
 * execSQLWithTrans
 *
 * @author LeonSu
 * @version 1.0.0.3
 * @date 2018-09-26
 */
public class DbUtility {


    /**
     * @param sql SQL文本
     * @return 扫行结果
     * @author LeonSu
     * @time $time$
     * @method 执行SQL语句
     * @version V1.0.0
     * @description
     */
    public static List<Map<String, Object>> execSQL(String sql) {
        Connection conn = DbConnection.getConnection();
        List<Map<String, Object>> list = new ArrayList<>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            MapListHandler handler = new MapListHandler();
            list = handler.handle(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbConnection.close(rs, st);
            DbConnection.close(conn);
            return list;
        }
    }

    /**
     * MethodName:execSQL
     * <p>
     * Description:带参数的查询
     * </p>
     *
     * @param sql    查询语句
     * @param params 参数值
     * @return
     * @author LeonSu
     */
    public static List<Map<String, Object>> execSQL(String sql, Object[] params) {
        Connection conn = DbConnection.getConnection();
        try {
            return execSQL(conn, sql, params);
        } finally {
            DbConnection.close(conn);
        }
    }

    /**
     * @param conn
     * @param sql
     * @return
     */
    public static List<Map<String, Object>> execSQL(Connection conn, String sql) {
        return execSQL(conn, sql, null);
    }

    /**
     * MethodName:execSQL
     * <p>
     * Description:带参数的查询
     * </p>
     *
     * @param conn   连接
     * @param sql    查询语句
     * @param params 参数值
     * @return
     * @author LeonSu
     */
    public static List<Map<String, Object>> execSQL(Connection conn,
                                                    String sql, Object[] params) {
        List<Map<String, Object>> list = new ArrayList<>();
        Statement st = null;
        ResultSet rs = null;
        try {
            if (params == null) {
                st = conn.createStatement();
                rs = st.executeQuery(sql);
            } else {
                st = conn.prepareStatement(sql);
                for (int i = 0; i < params.length; i++) {
                    ((PreparedStatement) st).setObject(i + 1, params[i]);
                }
                rs = ((PreparedStatement) st).executeQuery();
            }
            MapListHandler handler = new MapListHandler();
            list = handler.handle(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbConnection.close(rs, st);
            return list;
        }
    }

    /**
     * @param * @param null
     * @return
     * @author
     * @time $time$
     * @method
     * @version V1.0.0
     * @description
     */
    public static int execSQLWithTrans(String sql) {
        return execSQLWithTrans(sql, null, null);
    }

    /**
     * @param * @param null
     * @return
     * @author
     * @time $time$
     * @method
     * @version V1.0.0
     * @description
     */
    public static int execSQLWithTrans(Connection conn, String sql) {
        return execSQLWithTrans(conn, sql, (List<String>) null, null);
    }

    //根据ID批量删除
    public static int execSQLWithTrans(Connection conn, String sql, List<String> params) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                ps.setString(1, params.get(i).toString().trim());
                ps.executeUpdate();
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            DbConnection.close(ps);
        }
    }

    /**
     * 执行Update、Delete等SQL语句，带参数，使用事务 执行成功，返回0，否则返回1
     */
    public static int execSQLWithTrans(Connection conn, String sql,
                                       List<String> params, List<Map<String, Object>> values) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            if (values != null && params != null) {
                for (Map<String, Object> map : values) {
                    fillParam(ps, map, params);
                    ps.executeUpdate();
                }
            } else {
                ps.executeUpdate();
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return 1;
        } finally {
            DbConnection.close(ps);
        }
    }

    /**
     * @return 0：SQLServer；1：MySQL；2：Oracle
     */
    public static byte getDBType() {
        return DbConnection.getType();
    }

    public static ResultSetMetaData getResultSetMetaData(String sql) {
        Connection conn = DbConnection.getConnection();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.executeQuery();
            return ps.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DbConnection.close(ps);
            DbConnection.close(conn);
        }
    }

    public static int execSQLWithTrans(String sql,
                                       List<String> params, List<Map<String, Object>> values) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbConnection.getConnection();
            conn.setAutoCommit(false);
            if (execSQLWithTrans(conn, sql, params, values) == 0) {
                conn.commit();
                return 0;
            } else {
                conn.rollback();
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            return 1;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DbConnection.close(ps);
            DbConnection.close(conn);
        }
    }

    private static void fillParam(PreparedStatement ps, Map<String, Object> map, List<String> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            if (map.get(params.get(i)) == null) {
                ps.setString(i + 1, "");
            } else {
                ps.setString(i + 1, map.get(params.get(i)).toString()
                        .trim());
            }
        }
    }

    public static int execSQLWithTrans(Connection conn, String sql,
                                       List<ColumnInfo> colList, Map<String, Object> value) {
        List<String> params = new ArrayList<>();
        for (ColumnInfo columnInfo : colList) {
            params.add(columnInfo.getColumnName().toLowerCase().trim());
        }
        List<Map<String, Object>> values = new ArrayList<>();
        values.add(value);
        return execSQLWithTrans(conn, sql, params, values);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(DbUtility.execSQLWithTrans("DELETE FROM LOGS", (List<String>) null, null));
    }
}
