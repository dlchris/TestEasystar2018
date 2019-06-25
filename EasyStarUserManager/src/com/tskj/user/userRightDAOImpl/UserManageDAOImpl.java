package com.tskj.user.userRightDAOImpl;

import com.tskj.core.db.DbConnection;
import com.tskj.core.db.DbUtility;
import com.tskj.core.db.dao.DbBasicDao;
import com.tskj.core.db.dao.DbBasicDaoImpl;
import com.tskj.core.system.consts.DBConsts;
import com.tskj.core.system.utility.Tools;
import com.tskj.user.userRightDAO.UserManageDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManageDAOImpl implements UserManageDAO {

    /*private static Connection getConnection(){
        return DbConnection.getConnection("MsSql-config-right.properties");
    }*/

    @Override
    public List<Map<String, Object>> findAllUser(int pageIndex, int pageSize) {
        DbBasicDao dbdao = new DbBasicDaoImpl();
        //dbdao.
        String sql = "";
        List<Map<String, Object>> list = null;

        switch (DbUtility.getDBType()) {
            case DBConsts.DB_MYSQL:
                sql = "select USERID,USERNAME,NAME,DEPT,JOB,PROP from  reguserinfo ";
                list = dbdao.MySqlQueryPage(sql, pageSize, pageIndex);
                break;
            case DBConsts.DB_SQLSERVER:
                sql = "select USERID,USERNAME,NAME,DEPT,JOB,PROP,ROW_NUMBER() OVER(ORDER BY USERNAME) AS rowNum FROM reguserinfo";
                list = dbdao.MsSqlQuerypage(sql, pageSize, pageIndex);
                break;
            case DBConsts.DB_ORACLE:
                break;
            default:
                break;
        }
        return list;
    }

    @Override
    public int countUser() {
        String sql = "select count(*) as num from reguserinfo";
        DbBasicDao dbdao = new DbBasicDaoImpl();
        return dbdao.TotalRows(sql);
    }

    /**
     * @param
     * @Description: 根据用户名获取用户信息
     * @author Mao
     * @date 2019/1/28 15:36
     */
    @Override
    public List<Map<String, Object>> getUserInfo(String userName) {
        String sql = "SELECT * FROM REGUSERINFO WHERE USERNAME='" + userName + "'";
        return DbUtility.execSQL(sql);
    }

    /**
     * @param userName
     * @Description: 查询用户名是否存在  0:存在； 1：不存在；
     * @author Mao
     * @date 2019/1/28 15:38
     */
    @Override
    public String userExists(String userName) {
        String sql = "SELECT * FROM REGUSERINFO WHERE USERNAME='" + userName + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list == null || list.isEmpty()) {
            return "{\"code\":1,\"errMsg\":\"该用户不存在！\"}";
        }
        return "{\"code\":0,\"errMsg\":\"用户已存在！\"}";
    }

    /**
     * @param userInfo
     * @Description: 添加用户，包含验证   0:成功；1：失败；2：信息不全；3：用户名已存在
     * @author Mao
     * @date 2019/1/28 15:51
     */
    @Override
    public String addUser(Map<String, Object> userInfo) {
        if (userInfo.get("USERID") == null || "".equals(userInfo.get("USERID")) || userInfo.get("USERNAME") == null || "".equals(userInfo.get("USERNAME"))) {
            return "{\"code\":2,\"errMsg\":\"用户ID和用户名不能为空！\"}";
        }
        String userID = userInfo.get("USERID").toString();
        String sql = "SELECT * FROM REGUSERINFO WHERE USERID='" + userID + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list != null && !list.isEmpty()) {
            return "{\"code\":4,\"errMsg\":\"用户ID已存在！\"}";
        }
        String userName = userInfo.get("USERNAME").toString();
        sql = "SELECT * FROM REGUSERINFO WHERE USERNAME='" + userName + "'";
        List<Map<String, Object>> list2 = DbUtility.execSQL(sql);
        if (list2 != null && !list2.isEmpty()) {
            return "{\"code\":3,\"errMsg\":\"用户名已存在！\"}";
        }

        StringBuilder sqlKey = new StringBuilder();
        StringBuilder sqlValue = new StringBuilder();
        sqlKey.append("INSERT INTO REGUSERINFO (");
        sqlValue.append(") VALUES ( ");
        for (Map.Entry<String, Object> entry : userInfo.entrySet()) {
            sqlKey.append(entry.getKey() + ",");
            if (entry.getValue() == null) {
                sqlValue.append("'', ");
            } else {
                sqlValue.append("'" + entry.getValue() + "',");
            }
        }
        sql = sqlKey.substring(0, sqlKey.length() - 1) + sqlValue.substring(0, sqlValue.length() - 1) + ")";
        System.out.println(sql);
        int ret = DbUtility.execSQLWithTrans(sql);
        if (ret != 0) {
            return "{\"code\":1,\"errMsg\":\"添加用户失败！\"}";
        }
        return "{\"code\":0,\"errMsg\":\"添加用户成功！\"}";
    }


    /**
     * @param userID 用户ID
     * @Description: 删除用户
     * @author Mao
     * @date 2019/1/28 15:47
     */
    @Override
    public String delUser(String userID) throws SQLException {
        Connection conn = DbConnection.getConnection();
        String sql;
        conn.setAutoCommit(false);
        try {
            sql = "DELETE FROM REGUSERINFO WHERE USERID= '" + userID + "'";
            int ret = DbUtility.execSQLWithTrans(conn, sql);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"删除用户失败！\"}";
            }
            sql = "DELETE FROM USERRIGHT WHERE USERID= '" + userID + "'";
            ret = DbUtility.execSQLWithTrans(conn, sql);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"删除用户失败！\"}";
            }
            sql = "DELETE FROM USERMEMBER WHERE USERID= '" + userID + "'";
            ret = DbUtility.execSQLWithTrans(conn, sql);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"删除用户失败！\"}";
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
        return "{\"code\":0,\"errMsg\":\"删除用户成功！\"}";
    }

    @Override
    public boolean userCanDel(String userId) {
        String sql = "select  * from  reguserinfo where  USERID = '" + userId + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list != null && !list.isEmpty()) {
            String prop = Tools.toString(list.get(0).get("PROP"));
            if ("1".equals(prop) || "2".equals(prop)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param userID   用户ID， userInfo 用户信息
     * @param userInfo
     * @return 0:成功；1：失败；2：信息不全
     * @Description: 修改用户信息
     * @author Mao
     * @date 2019/1/28 15:54
     */
    @Override
    public String updateUserInfo(String userID, Map<String, Object> userInfo) throws SQLException {
        if (userID == null || "".equals(userID)) {
            return "{\"code\":2,\"errMsg\":\"用户ID不能为空！\"}";
        }
        //检查审计、系统用户是否是最后一个 1,2 审计和系统
        Connection conn = DbConnection.getConnection();
        conn.setAutoCommit(false);
        String findSql = "";

        try {
            switch (DbUtility.getDBType()) {
                case DBConsts.DB_MYSQL:
                     findSql = "select  count(prop) as num,PROP from  reguserinfo where  PROP =(select prop from reguserinfo where  USERID = '" + userID + "');";
                    break;
                case DBConsts.DB_SQLSERVER:
                     findSql = "select  count(prop) as num,PROP from  reguserinfo where  " +
                             "PROP =(select prop from reguserinfo where  USERID = '" + userID + "') GROUP BY PROP;";
                    break;
                default:
                break;
            }

            System.err.println(findSql);
            List<Map<String, Object>> list = DbUtility.execSQL(conn, findSql);
            Integer num = Integer.valueOf(list.get(0).get("num").toString());
            String prop = Tools.toString(list.get(0).get("PROP"));
            String newProp = Tools.toString(userInfo.get("PROP"));
            boolean flag = true;
            if (!prop.equals(newProp)) {
                switch (prop) {
                    case "1":
                    case "2":
                        if (num <= 1) {
                            flag = false;
                        }
                        break;
                    default:
                        break;
                }
            }
            if (flag) {
                StringBuilder sql = new StringBuilder();
                sql = sql.append("UPDATE REGUSERINFO SET ");
                for (Map.Entry<String, Object> entry : userInfo.entrySet()) {
                    if (entry.getValue() == null) {
                        sql = sql.append(entry.getKey() + "=" + "'', ");
                    } else {
                        sql = sql.append(entry.getKey() + "=" + "'" + entry.getValue() + "', ");
                    }
                }
                String sqlStr = sql.substring(0, sql.length() - 2) + " WHERE USERID = '" + userID + "'";
                System.out.println(sqlStr);
                int ret = DbUtility.execSQLWithTrans(conn, sqlStr);
                conn.commit();
                if (ret == 1) {
                    return "{\"code\":1,\"errMsg\":\"修改用户信息失败！\"}";
                }
            } else {
                return "{\"code\":1,\"errMsg\":\"该用户为最后一个审计或系统用户不能被修改删除\"}";
            }
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }

        return "{\"code\":0,\"errMsg\":\"修改用户信息成功！\"}";
    }

    /**
     * @param userID
     * @param menuID
     * @return
     * @Description: 验证用户权限
     * @author Mao
     * @date 2019/1/29 15:09
     */
    @Override
    public String checkOperationRight(String userID, String menuID) {
        String sql = "SELECT u.USERID, u.MENUID FROM USERRIGHT u WHERE "
                + "u.USERID='" + userID + "' AND u.MENUID='" + menuID + "' "
                + "UNION "
                + "SELECT um.USERID, r.MENUID FROM USERMEMBER um "
                + "LEFT JOIN ROLERIGHT r on um.ROLEID = r.ROLEID WHERE "
                + "um.USERID = '" + userID + "' AND r.MENUID = '" + menuID + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list == null || list.isEmpty()) {
            return "{\"code\":1,\"errMsg\":\"该权限不存在！\"}";
        }
        return "{\"code\":0,\"errMsg\":\"用户有该权限！\"}";
    }

    /**
     * @param userID
     * @return
     * @Description: 获取用户权限
     * @author Mao
     * @date 2019/1/29 15:10
     */
    @Override
    public List<Map<String, Object>> getUserRight(String userID) {
        String sql = "SELECT USERRIGHT.USERID, MENUINFO.* FROM USERRIGHT LEFT JOIN MENUINFO ON MENUINFO.MENUID=USERRIGHT.MENUID WHERE USERRIGHT.USERID='" + userID + "' "
                + "UNION "
                + "SELECT USERMEMBER.USERID, MENUINFO.* FROM USERMEMBER, ROLERIGHT, MENUINFO WHERE USERMEMBER.ROLEID = ROLERIGHT.ROLEID AND ROLERIGHT.MENUID = MENUINFO.MENUID AND USERMEMBER.USERID='" + userID + "'";
//        System.out.println(sql);
        return DbUtility.execSQL(sql);
    }

    /**
     * @param
     * @return
     * @Description: 根据菜单类型获取权限
     * @author Mao
     * @date 2019/2/13 10:55
     */
    @Override
    public List<Map<String, Object>> getUserRightByType(String userID, String menuType) {
        String sql = "SELECT MENUINFO.* FROM USERRIGHT LEFT JOIN MENUINFO ON MENUINFO.MENUID=USERRIGHT.MENUID WHERE USERRIGHT.USERID='" + userID + "' AND MENUINFO.MENUTYPE = '" + menuType + "' "
                + "UNION "
                + "SELECT USERMEMBER.USERID, MENUINFO.* FROM USERMEMBER, ROLERIGHT, MENUINFO WHERE USERMEMBER.ROLEID = ROLERIGHT.ROLEID AND ROLERIGHT.MENUID = MENUINFO.MENUID AND USERMEMBER.USERID='" + userID + "' AND MENUINFO.MENUTYPE = '" + menuType + "'";
//        System.out.println(sql);
        return DbUtility.execSQL(sql);
    }

    /**
     * @param
     * @return
     * @Description: 根据菜单类型获取父级菜单列表
     * @author Mao
     * @date 2019/2/13 16:17
     */
    @Override
    public List<Map<String, Object>> getMenuByType(String userID, String roleID, String menuType) {
        String sql = "SELECT MENUINFO.* FROM USERRIGHT LEFT JOIN MENUINFO ON " +
                "USERRIGHT.MENUID = MENUINFO.MENUID WHERE USERRIGHT.USERID = '" + userID + "' AND MENUINFO.MENUTYPE = '" + menuType + "' " +
                "UNION " +
                "SELECT MENUINFO.* FROM ROLERIGHT LEFT JOIN MENUINFO ON " +
                "ROLERIGHT.MENUID = MENUINFO.MENUID WHERE ROLERIGHT.ROLEID = '" + roleID + "' AND MENUINFO.MENUTYPE = '" + menuType + "'";
//        System.out.println(sql);
        return DbUtility.execSQL(sql);
    }

    /**
     * @return
     * @Description: 获取所有菜单信息
     * @author Mao
     * @date 2019/1/29 15:13
     */
    @Override
    public List<Map<String, Object>> getMenuInfo() {
        String sql = "SELECT * FROM MENUINFO ORDER BY MENUTYPE";
        return DbUtility.execSQL(sql);
    }

    /**
     * @param menuType
     * @return
     * @Description: 根据menuType查询菜单
     * @author Mao
     * @date 2019/1/30 13:54
     */
    @Override
    public List<Map<String, Object>> getMenuInfo(String menuType) {
        String sql = "SELECT * FROM MENUINFO where menutype = '" + menuType + "'";
        return DbUtility.execSQL(sql);
    }

    /**
     * @param userID
     * @param userRights
     * @return
     * @Description: 重新设置用户权限，先清除原权限，添加新的权限集合
     * @author Mao
     * @date 2019/1/29 15:18
     */
    @Override
    public String resetUserRight(String userID, List<Map<String, Object>> userRights) throws SQLException {
        List<String> params = new ArrayList<String>();
        params.add("USERID");
        params.add("MENUID");
        Connection conn = DbConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            String sql = "DELETE FROM USERRIGHT WHERE USERID='" + userID + "'";
            int ret = DbUtility.execSQLWithTrans(conn, sql);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"重新分配用户权限失败！\"}";
            }
            sql = "INSERT INTO USERRIGHT (USERID, MENUID) VALUES (?,?)";
            ret = DbUtility.execSQLWithTrans(conn, sql, params, userRights);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"重新分配用户权限失败！\"}";
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
        return "{\"code\":0,\"errMsg\":\"重新分配用户权限成功！\"}";
    }

    /**
     * @param userID
     * @param menuID
     * @return
     * @Description: 删除用户某权限
     * @author Mao
     * @date 2019/1/29 15:21
     */
    @Override
    public String delUserRight(String userID, String menuID) {
        String sql = "DELETE FROM USERRIGHT WHERE USERID='" + userID + "' AND MENUID = '" + menuID + "'";
        int ret = DbUtility.execSQLWithTrans(sql);
        if (ret == 1) {
            return "{\"code\":1,\"errMsg\":\"用户权限删除失败！\"}";
        }
        return "{\"code\":0,\"errMsg\":\"用户权限删除成功！\"}";
    }

    /**
     * @param
     * @return
     * @Description: 添加用户权限
     * @author Mao
     * @date 2019/1/29 15:23
     */
    @Override
    public String addUserRight(String userID, String menuID) {
        if (userID == null || "".equals(userID) || menuID == null || "".equals(menuID)) {        //验证信息是否为空
            return "{\"code\":2,\"errMsg\":\"参数不能为空！\"}";
        }
        String sql = "SELECT u.USERID, u.MENUID FROM USERRIGHT u WHERE "
                + "u.USERID='" + userID + "' AND u.MENUID='" + menuID + "' "
                + "UNION "
                + "SELECT um.USERID, r.MENUID FROM USERMEMBER um "
                + "LEFT JOIN ROLERIGHT r on um.ROLEID = r.ROLEID WHERE "
                + "um.USERID = '" + userID + "' AND r.MENUID = '" + menuID + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list != null && !list.isEmpty()) {
            return "{\"code\":3,\"errMsg\":\"用户权限已存在！\"}";
        }
        sql = "INSERT INTO USERRIGHT (USERID, MENUID) VALUES ('" + userID + "', '" + menuID + "')";

        int ret = DbUtility.execSQLWithTrans(sql);
        if (ret == 1) {
            return "{\"code\":1,\"errMsg\":\"添加权限失败！\"}";
        }
        return "{\"code\":0,\"errMsg\":\"添加权限成功！\"}";
    }

    /**
     * @param
     * @return
     * @Description: 检测角色名是否已存在
     * @author Mao
     * @date 2019/1/30 14:33
     */
    @Override
    public int roleExists(String roleName) {
        String sql = "SELECT * FROM ROLEINFO WHERE ROLENAME='" + roleName + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list == null || list.isEmpty()) {
            return 1;
        }
        return 0;
    }

    /*******************************************************************************************************/

    /**
     * @param roleInfo
     * @return 0：成功； 1：失败； 2：信息有空值; 3:角色名已存在; 4:ROLEID已存在
     * @Description: 添加角色
     * @author Mao
     * @date 2019/1/29 16:00
     */
    @Override
    public String addRole(Map<String, Object> roleInfo) {
        if (roleInfo.get("ROLEID") == null || "".equals(roleInfo.get("ROLEID").toString()) || roleInfo.get("ROLENAME") == null || "".equals(roleInfo.get("ROLENAME").toString())) {
            return "{\"code\":2,\"errMsg\":\"角色ID和角色名不能为空！\"}";
        }
        if (roleExists(roleInfo.get("ROLENAME").toString()) == 0) {   //验证角色名是否已存在
            return "{\"code\":3,\"errMsg\":\"角色名已存在！\"}";
        }
        String sql = "SELECT * FROM ROLEINFO WHERE ROLEID = '" + roleInfo.get("ROLEID") + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list != null && !list.isEmpty()) {
            return "{\"code\":4,\"errMsg\":\"角色ID已存在！\"}";
        }
        String roleMemo = "";
        if (roleInfo.get("ROLEMEMO") != null && !"".equals(roleInfo.get("ROLEMEMO"))) {
            roleMemo = roleInfo.get("ROLEMEMO").toString();
        }
        sql = "INSERT INTO ROLEINFO (ROLEID, ROLENAME, ROLEMEMO) VALUES ('"
                + roleInfo.get("ROLEID") + "', '" + roleInfo.get("ROLENAME") + "', '" + roleMemo + "')";
        System.out.println(sql);
        int ret = DbUtility.execSQLWithTrans(sql);
        if (ret == 1) {
            return "{\"code\":1,\"errMsg\":\"添加角色失败！\"}";
        }
        return "{\"code\":0,\"errMsg\":\"添加角色成功！\"}";
    }

    /**
     * @param roleID
     * @return
     * @Description: 删除角色  TODO 修改 rolemodule,roleright没有删除会出现问题
     * @author Mao, JRX
     * @date 2019/1/29 16:04
     */
    @Override
    public String delRole(String roleID) throws SQLException {
        Connection conn = DbConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            String sql = "DELETE FROM USERMEMBER WHERE ROLEID = '" + roleID + "'";
            int ret = DbUtility.execSQLWithTrans(conn, sql);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"删除角色失败！\"}";
            }
            sql = "DELETE FROM ROLEINFO WHERE ROLEID='" + roleID + "'";
            ret = DbUtility.execSQLWithTrans(conn, sql);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"删除角色失败！\"}";
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
        return "{\"code\":0,\"errMsg\":\"删除角色成功！\"}";
    }

    /**
     * @param roleInfo
     * @return 0：成功； 1：失败； 2：信息有空值
     * @Description: 修改角色信息
     * @author Mao
     * @date 2019/1/29 16:10
     */
    @Override
    public String updateRoleInfo(Map<String, Object> roleInfo) {
        if (roleInfo.get("ROLEID") == null || "".equals(roleInfo.get("ROLEID").toString()) || roleInfo.get("ROLENAME") == null || "".equals(roleInfo.get("ROLENAME").toString())) {
            return "{\"code\":2,\"errMsg\":\"角色ID和角色名不能为空！\"}";
        }
        StringBuilder sql = new StringBuilder();
        sql = sql.append("UPDATE ROLEINFO SET ");
        for (Map.Entry<String, Object> entry : roleInfo.entrySet()) {
            if (entry.getValue() == null) {
                sql = sql.append(entry.getKey() + "=" + "'', ");
            } else {
                sql = sql.append(entry.getKey() + "=" + "'" + entry.getValue() + "', ");
            }
        }
        String sqlStr = sql.substring(0, sql.length() - 2) + " WHERE ROLEID = '" + roleInfo.get("ROLEID") + "'";
        System.out.println(sqlStr);
        int ret = DbUtility.execSQLWithTrans(sqlStr);
        if (ret == 1) {
            return "{\"code\":1,\"errMsg\":\"修改角色失败！\"}";
        }
        return "{\"code\":0,\"errMsg\":\"修改角色成功！\"}";
    }

    /**
     * @param roleID
     * @return
     * @Description: 获取角色信息
     * @author Mao
     * @date 2019/1/29 16:06
     */
    @Override
    public Map<String, Object> getRoleInfo(String roleID) {
        String sql = "SELECT * FROM ROLEINFO WHERE ROLEID = '" + roleID + "'";
        List<Map<String, Object>> roleInfo = DbUtility.execSQL(sql);
        if (roleInfo != null && !roleInfo.isEmpty()) {
            return roleInfo.get(0);
        }
        return null;
    }

    /**
     * @return
     * @Description: 获取角色集合
     * @author Mao
     * @date 2019/1/29 16:05
     */
    @Override
    public List<Map<String, Object>> getRoleList() {
        String sql = "SELECT * FROM ROLEINFO";
        return DbUtility.execSQL(sql);
    }

    /**
     * @param roleID
     * @param roleRights
     * @return
     * @Description: 设定/重新设定角色权限
     * @author Mao
     * @date 2019/1/29 16:11
     */
    @Override
    public String resetRoleRight(String roleID, List<Map<String, Object>> roleRights) throws SQLException {
        List<String> params = new ArrayList<String>();
        params.add("ROLEID");
        params.add("MENUID");
        Connection conn = DbConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            String sql = "DELETE FROM ROLERIGHT WHERE ROLEID='" + roleID + "'";
            int ret = DbUtility.execSQLWithTrans(conn, sql);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"修改角色权限失败！\"}";
            }
            sql = "INSERT INTO ROLERIGHT (ROLEID, MENUID) VALUES (?, ?)";
            ret = DbUtility.execSQLWithTrans(conn, sql, params, roleRights);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"修改角色权限失败！\"}";
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
        return "{\"code\":0,\"errMsg\":\"修改角色权限成功！\"}";
    }

    /**
     * @param roleID
     * @return
     * @Description: 获取角色权限
     * @author Mao
     * @date 2019/1/29 16:14
     */
    @Override
    public List<Map<String, Object>> getRoleRight(String roleID) {
        String sql = "SELECT ROLERIGHT.ROLEID, MENUINFO.* FROM ROLERIGHT LEFT JOIN MENUINFO ON ROLERIGHT.MENUID=MENUINFO.MENUID WHERE ROLERIGHT.ROLEID='"
                + roleID + "' ORDER BY ROLERIGHT.ROLEID";
        return DbUtility.execSQL(sql);
    }

    @Override
    public List<Map<String, Object>> getRoleRightByMenuType(String roleID, String menuType) {
        String sql = "SELECT r.ROLEID,m.* FROM MENUINFO m INNER JOIN roleright r ON r.MENUID = m.MENUID" +
                "  where m.MENUTYPE = '" + menuType + "' AND r.ROLEID = '" + roleID + "'";
        return DbUtility.execSQL(sql);
    }

    /**
     * @param roleID
     * @return
     * @Description: 获取某角色下用户信息
     * @author Mao
     * @date 2019/1/29 16:15
     */
    @Override
    public List<Map<String, Object>> getUsersByRoleID(String roleID) {
        if (roleID != null && !roleID.isEmpty()) {
            String sql = "SELECT REGUSERINFO.* FROM REGUSERINFO LEFT JOIN USERMEMBER ON " +
                    "USERMEMBER.USERID=REGUSERINFO.USERID WHERE ROLEID='" + roleID + "' ORDER BY USERNAME";

            return DbUtility.execSQL(sql);
        }
        return null;
    }

    /**
     * @param roleID
     * @param menuID
     * @return
     * @Description: 验证角色权限
     * @author Mao
     * @date 2019/1/29 16:15
     */
    @Override
    public String checkRoleRight(String roleID, String menuID) {
        String sql = "SELECT * FROM ROLERIGHT WHERE ROLEID = '" + roleID
                + "' AND MENUID = '" + menuID + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list == null || list.isEmpty()) {
            return "{\"code\":1,\"errMsg\":\"角色没有改权限！\"}";
        }
        return "{\"code\":0,\"errMsg\":\"角色有该权限！\"}";
    }

    /**
     * @param roleID
     * @param menuID
     * @return
     * @Description: 删除角色某权限
     * @author Mao
     * @date 2019/1/29 16:17
     */
    @Override
    public String delRoleRight(String roleID, String menuID) {
        String sql = "DELETE FROM ROLERIGHT WHERE ROLEID='" + roleID
                + "' AND MENUID = '" + menuID + "'";
        int ret = DbUtility.execSQLWithTrans(sql);
        if (ret == 1) {
            return "{\"code\":1,\"errMsg\":\"删除角色权限失败！\"}";
        }
        return "{\"code\":0,\"errMsg\":\"删除角色权限成功！\"}";
    }

    /**
     * @param
     * @return 0：成功； 1：失败； 2：信息有空值
     * @Description: 赋予角色某权限
     * @author Mao
     * @date 2019/1/29 16:18
     */
    @Override
    public String addRoleRight(String roleID, String menuID) {
        String sql = "SELECT * FROM ROLERIGHT WHERE ROLEID = '" + roleID + "' AND MENUID = '" + menuID + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list != null && !list.isEmpty()) {
            return "{\"code\":2,\"errMsg\":\"权限已存在！\"}";
        }

        sql = "INSERT INTO ROLERIGHT (ROLEID, MENUID) VALUES ('" + roleID + "', '" + menuID + "')";
        int ret = DbUtility.execSQLWithTrans(sql);
        if (ret == 1) {
            return "{\"code\":1,\"errMsg\":\"添加角色权限失败！\"}";
        }
        return "{\"code\":0,\"errMsg\":\"添加角色权限成功！\"}";
    }

    /**
     * @param userRoles
     * @return
     * @Description: 为用户分配/重置角色
     * @author Mao
     * @date 2019/1/29 16:19
     */
    @Override
    public String resetUserRole(String userID, List<Map<String, Object>> userRoles) throws SQLException {
        List<String> params = new ArrayList<String>();
        params.add("USERID");
        params.add("ROLEID");
        Connection conn = DbConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            String sql = "DELETE FROM USERMEMBER WHERE USERID = '" + userID + "'";
            int ret = DbUtility.execSQLWithTrans(conn, sql);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"修改用户角色失败！\"}";
            }
            sql = "INSERT INTO USERMEMBER (USERID, ROLEID) VALUES (?,?)";
            ret = DbUtility.execSQLWithTrans(conn, sql, params, userRoles);
            if (ret == 1) {
                conn.rollback();
                return "{\"code\":1,\"errMsg\":\"修改用户角色失败！\"}";
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
        return "{\"code\":0,\"errMsg\":\"修改角色权限成功！\"}";
    }

    /**
     * @param userID
     * @return
     * @Description: 获取用户所属角色信息
     * @author Mao
     * @date 2019/1/29 16:23
     */
    @Override
    public List<Map<String, Object>> getUserRole(String userID) {
        String sql = "SELECT REGUSERINFO.USERID, REGUSERINFO.USERNAME,ROLEINFO.ROLEID, ROLEINFO.ROLENAME FROM REGUSERINFO, ROLEINFO, USERMEMBER WHERE USERMEMBER.USERID = REGUSERINFO.USERID AND USERMEMBER.ROLEID = ROLEINFO.ROLEID AND USERMEMBER.USERID = '" + userID + "'";
        return DbUtility.execSQL(sql);
    }

    /**
     * @param userID
     * @param roleID
     * @return
     * @Description: 获取用户可操作门类
     * @author Mao
     * @date 2019/2/13 14:33
     */
    @Override
    public List<Map<String, Object>> getClassTree(String userID, String roleID) {
        String sql = "SELECT REPLACE(NEWID(), '-', '') as 'newClassId', CLASSID, DESCRIPTION, PERFIXDES, DOCTABLE, BOXTABLE, ROOLTABLE, " +
                "dbo.GetClassType (DOCTABLE, BOXTABLE, ROOLTABLE) as 'CLASSTYPE' from CLASSTREE LEFT JOIN " +
                "USERMODULE on CLASSTREE.CLASSID = USERMODULE.MODULEID WHERE  CLASSLEVEL='7' AND CLASSTYPE=1 and (USERMODULE.USERID = '" + userID + "' " +
                "or CLASSTREE.CLASSID in (SELECT MODULEID from ROLEMODULE WHERE ROLEMODULE.ROLEID = '" + roleID + "'))";
        System.out.println(sql);
        return DbUtility.execSQL(sql);
    }

    /**
     * @param userID
     * @return
     * @Description: 获取用户可操作门类
     * @author Mao
     * @date 2019/2/13 15:16
     */
    @Override
    public List<Map<String, Object>> getClassTree(String userID) {
        String sql = "SELECT REPLACE(NEWID(), '-', '') as 'newClassId', CLASSID, DESCRIPTION, PERFIXDES, DOCTABLE, BOXTABLE, ROOLTABLE, " +
                "dbo.GetClassType (DOCTABLE, BOXTABLE, ROOLTABLE) as 'CLASSTYPE' from CLASSTREE LEFT JOIN " +
                "USERMODULE on CLASSTREE.CLASSID = USERMODULE.MODULEID WHERE  CLASSLEVEL='7' AND CLASSTYPE=1 and (USERMODULE.USERID = '" + userID + "' " +
                "or CLASSTREE.CLASSID in (SELECT MODULEID from ROLEMODULE WHERE ROLEMODULE.ROLEID in " +
                "(SELECT roleid from USERMEMBER WHERE userid = '" + userID + "')))";
        System.out.println(sql);
        return DbUtility.execSQL(sql);
    }

    /**
     * @param userID   用户ID
     * @param roleID   角色ID
     * @param menuType 菜单类型
     * @param mainMenu 主菜单名称，根据名称查对应的子菜单内容
     * @return
     * @Description: 获取功能操作权限
     * @author Mao
     * @date 2019/2/14 10:51
     */
    @Override
    public List<Map<String, Object>> getOperationRight(String userID, String roleID, String menuType, String mainMenu) {
        String sql = "SELECT MENUINFO.MENUID FROM USERRIGHT LEFT JOIN MENUINFO ON " +
                "USERRIGHT.MENUID = MENUINFO.MENUID WHERE USERRIGHT.USERID = '" + userID + "' AND MENUINFO.MENUTYPE = '" + menuType + "' " +
                "AND MENUINFO.MAINMENU = '" + mainMenu + "'" +
                " UNION " +
                "SELECT MENUINFO.MENUID FROM ROLERIGHT LEFT JOIN MENUINFO ON " +
                "ROLERIGHT.MENUID = MENUINFO.MENUID WHERE ROLERIGHT.ROLEID = '" + roleID + "' AND MENUINFO.MENUTYPE = '" + menuType + "' " +
                "AND MENUINFO.MAINMENU = '" + mainMenu + "'";
        //System.out.println(sql);
        return DbUtility.execSQL(sql);
    }

    /**
     * @param
     * @return
     * @Description: 获取指定权限
     * @author Mao
     * @date 2019/2/15 9:14
     */
    @Override
    public List<Map<String, Object>> getAppointRight(String userID, String roleID, String menuID) {
        String sql = "select menuinfo.menuid from userright left join menuinfo on " +
                "userright.menuid = menuinfo.menuid where userright.userid = '" + userID + "' and menuinfo.menuid = '" + menuID + "'" +
                " union " +
                "select menuinfo.menuid from roleright left join menuinfo on " +
                "roleright.menuid = menuinfo.menuid where roleright.roleid = '" + roleID + "' and menuinfo.menuid = '" + menuID + "'";
        return DbUtility.execSQL(sql);
    }

    @Override
    public int changePassWord(String userId, String newPass) {
        String sql = "update reguserinfo  set userpw = '" + newPass + "'  where userid = '" + userId + "'";
        return DbUtility.execSQLWithTrans(sql);
    }

    @Override
    public List<Map<String, Object>> getUserRightByMenuType(String userId, String menuType) {
        String sql = "SELECT * FROM MENUINFO m INNER JOIN USERRIGHT u ON u.MENUID = m.MENUID " +
                "WHERE m.MENUTYPE = '" + menuType + "' AND u.USERID = '" + userId + "'";
        System.err.println(sql);
        return DbUtility.execSQL(sql);
    }

    @Override
    public List<Map<String, Object>> getUserBoundRoleByUserId(String userId) {
        String sql = "SELECT * FROM USERMEMBER WHERE USERID = '" + userId + "'";
        return DbUtility.execSQL(sql);
    }

    @Override
    public int saveUserBoundRole(String userId, List<Map<String, Object>> userMember) throws SQLException {
        List<String> params = new ArrayList<String>();
        params.add("ROLEID");
        params.add("USERID");
        Connection conn = DbConnection.getConnection();
        conn.setAutoCommit(false);
        //角色ID对应的用户 union + 剩下的用户集合去掉超出角色数量总数
        try {
            String sql = "DELETE FROM USERMEMBER WHERE USERID = '" + userId + "'";
            int ret = DbUtility.execSQLWithTrans(conn, sql);
            if (ret == 1) {
                conn.rollback();
                return 1;
            }

            sql = "INSERT INTO USERMEMBER (ROLEID, USERID) VALUES (?, ?)";
            ret = DbUtility.execSQLWithTrans(conn, sql, params, userMember);
            if (ret == 1) {
                conn.rollback();
                return 1;
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
        return 0;
    }

    @Override
    public List<Map<String, Object>> userCTreePower(String classId, String userId) {
        String sql = "SELECT * FROM USERMODULE WHERE USERID = '" + userId + "' AND MODULEID= '" + classId + "'";
        return DbUtility.execSQL(sql);
    }

    @Override
    public Map<String, Object> getFieldType(String classId, String fieldName) {
        String sql = "SELECT DICTTABLE FROM DOCFRAME WHERE classid = '" + classId + "' AND FIELDNAME = '" + fieldName + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        Map<String, Object> map = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            map = list.get(0);
        }
        return map;
    }

    @Override
    public List<Map<String, Object>> getSysDictFieldList(String fieldName) {
        String sql = "SELECT DICTID,DVALUE,FIELDNAME FROM SYSDICT ";
        sql += " WHERE FIELDNAME='" + fieldName + "' ";
        sql += " ORDER BY DICTID ";
        System.err.println(sql);
        return DbUtility.execSQL(sql);
    }

    @Override
    public List<Map<String, Object>> getUserDictFieldList(String classId, String fieldName) {


        return null;
    }

    @Override
    public List<Map<String, Object>> findUserModuleById(Connection conn,String userId, String moduleId) {
        String sql = "SELECT * FROM usermodule  WHERE userId = '" + userId + "' AND MODULEID = '" + moduleId + "'";
        return DbUtility.execSQL(conn,sql);
    }

    @Override
    public int addUserModule(Connection conn,String userId, String moduleId, String security, String security2) {
        String sql = "insert into usermodule(userid,moduleid,security,security2) " +
                " values('" + userId + "','" + moduleId + "','" + security + "','" + security2 + "')";
        return DbUtility.execSQLWithTrans(conn,sql);
    }

    @Override
    public int updateUserModule(Connection conn,String userId, String moduleId, String security, String security2) {
        String sql = "update usermodule set security = '" + security + "',security2 = '" + security2 + "' " +
                " where userId = '" + userId + "' and moduleId = '" + moduleId + "'";
        return DbUtility.execSQLWithTrans(conn,sql);
    }


    public static void main(String[] args) {
        UserManageDAO u = new UserManageDAOImpl();
        //int i = u.changePassWord("8E1D012469B041A1862B6E07C1ABC5A3", "123");
    }

}
