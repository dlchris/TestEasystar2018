package com.tskj.role.dao.impl;

import com.sun.istack.internal.NotNull;
import com.tskj.core.db.DbConnection;
import com.tskj.core.db.DbUtility;
import com.tskj.core.db.dao.DbBasicDao;
import com.tskj.core.db.dao.DbBasicDaoImpl;
import com.tskj.core.system.consts.DBConsts;
import com.tskj.core.system.utility.Tools;
import com.tskj.role.dao.RoleManagerDao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-25 10:51
 **/
public class RoleManagerDaoImpl implements RoleManagerDao {
    @Override
    public List<Map<String, Object>> findAllRole(int pageIndex, int pageSize) {
        DbBasicDao dbdao = new DbBasicDaoImpl();
        List<Map<String, Object>> list = null;
        String sql = "";
        switch (DbUtility.getDBType()) {
            case DBConsts.DB_MYSQL:
                sql = "select * from roleinfo ";
                list = dbdao.MySqlQueryPage(sql, pageSize, pageIndex);
                break;
            case DBConsts.DB_SQLSERVER:
                sql = "select *,ROW_NUMBER() OVER(ORDER BY ROLENAME) AS rowNum FROM roleinfo";
                list = dbdao.newMsSqlQuerypage(sql, pageSize, pageIndex);
                break;
            case DBConsts.DB_ORACLE:
                break;
            default:
                break;
        }
        return list;
    }


    @Override
    public int countRole() {
        String sql = "select count(*) as num from  roleinfo";
        DbBasicDao dbBasicDao = new DbBasicDaoImpl();
        return dbBasicDao.TotalRows(sql);
    }

    @Override
    public List<Map<String, Object>> findRoleNameUnique(String roleName) {
        String sql = "select * from rolename where rolename = '" + roleName + "'";
        return DbUtility.execSQL(sql);
    }

    @Override
    public int addRole(Map<String, Object> params) {
        String sql = "insert into roleinfo(roleid,rolename,rolememo) " +
                "value('" + Tools.newId() + "','" + Tools.toString(params.get("roleName")) + "','" + Tools.toString(params.get("roleMemo")) + "') ";
        return DbUtility.execSQLWithTrans(sql);
    }

    @Override
    public int updateRole(Map<String, Object> param, String roleId) {
        String sql = "update  roleinfo set " + getUpdateFields(param) + " where roleId = '" + roleId + "'";
        return DbUtility.execSQLWithTrans(sql);
    }

    private String getUpdateFields(Map<String, Object> param) {
        String tmpStr = "";
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            tmpStr += entry.getKey() + " = " + entry.getValue() + " , ";
        }
        tmpStr = tmpStr.substring(0, tmpStr.length() - 2);
        System.err.println(tmpStr);
        return tmpStr;
    }

    @Override
    public int deleteRole() {

        return 0;
    }

    @Override
    public int roleAssignment(String roleId, List<Map<String, Object>> userMember) throws SQLException {
        List<String> params = new ArrayList<String>();
        params.add("ROLEID");
        params.add("USERID");
        Connection conn = DbConnection.getConnection();
        conn.setAutoCommit(false);
        //角色ID对应的用户 union + 剩下的用户集合去掉超出角色数量总数
        //todo 删除之前还是要先验证用户绑定角色的个数是否超过最大数量
        try {
            String sql = "DELETE FROM USERMEMBER WHERE ROLEID = '" + roleId + "'";
            int ret = DbUtility.execSQLWithTrans(conn, sql);
            if (ret == 1) {
                conn.rollback();
                //return "{\"result\":1,\"msg\":\"修改角色权限失败！\"}";
                return 1;
            }

            sql = "INSERT INTO USERMEMBER (ROLEID, USERID) VALUES (?, ?)";
            ret = DbUtility.execSQLWithTrans(conn, sql, params, userMember);
            if (ret == 1) {
                conn.rollback();
                //return "{\"result\":1,\"msg\":\"修改角色权限失败！\"}";
                return 1;
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
        //return "{\"result\":0,\"msg\":\"修改角色权限成功！\"}";
        return 0;
    }

    @Override
    public List<Map<String, Object>> roleBoundUsersList(String roleId) {
        String sql = "SELECT rg.USERID,rg.USERNAME,rg.NAME,rg.DEPT FROM reguserinfo  " +
                "rg LEFT JOIN usermember u ON rg.USERID = u.USERID WHERE u.ROLEID= '" + roleId + "' ";
        return DbUtility.execSQL(sql);
    }

    @Override
    public List<Map<String, Object>> roleCanBoundUsers(String roleId) {
        ResourceBundle rsb = ResourceBundle.getBundle("/config/db/systemSetup");
        String num = rsb.getString("UserBindRolesNum");
        String sql = "";
        switch (DbUtility.getDBType()) {
            case DBConsts.DB_MYSQL:
                sql = "SELECT rg.USERID,rg.USERNAME,rg.NAME,rg.DEPT FROM reguserinfo  rg LEFT JOIN usermember u ON" +
                        " rg.USERID = u.USERID WHERE u.ROLEID= '" + roleId + "'  AND rg.PROP = 0 " +
                        " UNION  " +
                        " SELECT rg.USERID,rg.USERNAME,rg.NAME,rg.DEPT FROM reguserinfo  rg LEFT JOIN usermember u ON" +
                        " rg.USERID = u.USERID  WHERE ifnull(u.ROLEID,'') = '' AND rg.PROP = 0 " +
                        " UNION " +
                        " SELECT rg.USERID,rg.USERNAME,rg.NAME,rg.DEPT FROM reguserinfo  rg LEFT JOIN usermember u ON" +
                        " rg.USERID = u.USERID  WHERE ifnull(u.ROLEID,'') <>'' AND rg.PROP = 0  GROUP BY rg.USERID  HAVING count(*)<'" + num + "'";
                break;
            case DBConsts.DB_SQLSERVER:
                sql = "SELECT rg.USERID,rg.USERNAME,rg.NAME,rg.DEPT FROM reguserinfo  rg LEFT JOIN usermember u ON" +
                        " rg.USERID = u.USERID WHERE u.ROLEID= '" + roleId + "'  AND rg.PROP = 0 " +
                        " UNION  " +
                        " SELECT rg.USERID,rg.USERNAME,rg.NAME,rg.DEPT FROM reguserinfo  rg LEFT JOIN usermember u ON" +
                        " rg.USERID = u.USERID  WHERE isnull(u.ROLEID,'') = ''  AND rg.PROP = 0 " +
                        " UNION " +
                        " SELECT rg.USERID,rg.USERNAME,rg.NAME,rg.DEPT FROM REGUSERINFO rg WHERE " +
                        " EXISTS(SELECT r.USERID FROM reguserinfo  r LEFT JOIN usermember u ON r.USERID = u.USERID  WHERE " +
                        " rg.USERID = r.USERID AND isnull(u.ROLEID,'') <>'' AND r.PROP = 0  GROUP BY r.USERID  HAVING count(r.USERID)<'" + num + "')";
                break;
            case DBConsts.DB_ORACLE:
            default:
                break;
        }
        System.err.println(sql);
        return DbUtility.execSQL(sql);
    }

    @Override
    public int boundRoleNumByUserID(String userId) {
        String sql = "";
        return 0;
    }

    @Override
    public List<Map<String, Object>> getSysDict(String fieldName) {
        String sql = "SELECT DICTID,DVALUE,FIELDNAME FROM SYSDICT";
        if (!fieldName.isEmpty()) {
            sql += " WHERE FIELDNAME='" + fieldName + "'";
        }
        sql += " ORDER BY FIELDNAME, DICTID";
        return DbUtility.execSQL(sql);
    }
    @Override
    public List<Map<String, Object>> FindDIctField(String classId, String fieldName) {
        //测试 查门类是系统字段还是用户字段
        String sql = "select  TABLENAME,DICTTABLE from  docframe where  CLASSID = '" + classId + "' " +
                "and FIELDNAME = '" + fieldName + "' order by  TABLENAME desc ";
        System.err.println(sql);
        return DbUtility.execSQL(sql);
    }
    @Override
    public List<Map<String, Object>> findRoleModuleById(String roleId, String moduleId) {
        String sql = "SELECT * FROM ROLEMODULE  WHERE ROLEID = '" + roleId + "' AND MODULEID = '" + moduleId + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        /*Map<String, Object> map = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            map = list.get(0);
        }*/
        return list;
    }

    @Override
    public int addRoleModule(String roleId, String moduleId, String security, String security2) {
        String sql = "insert into rolemodule(roleid,moduleid,security,security2) " +
                " values('" + roleId + "','" + moduleId + "','" + security + "','" + security2 + "')";
        return DbUtility.execSQLWithTrans(sql);
    }

    @Override
    public int updateRoleModule(String roleId, String moduleId, String security, String security2) {
        String sql = "update rolemodule set security = '" + security + "',security2 = '" + security2 + "' " +
                " where roleid = '" + roleId + "' and moduleId = '" + moduleId + "'";
        return DbUtility.execSQLWithTrans(sql);
    }
}
