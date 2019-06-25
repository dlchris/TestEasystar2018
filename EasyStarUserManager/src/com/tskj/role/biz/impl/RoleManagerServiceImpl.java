package com.tskj.role.biz.impl;

import com.alibaba.fastjson.JSONObject;
import com.tskj.core.db.DbUtility;
import com.tskj.core.system.utility.Tools;
import com.tskj.docframe.dao.DictManager;
import com.tskj.role.biz.RoleManagerService;
import com.tskj.role.dao.RoleManagerDao;
import com.tskj.role.dao.impl.RoleManagerDaoImpl;
import com.tskj.user.userRightDAO.UserManageDAO;
import com.tskj.user.userRightDAOImpl.UserManageDAOImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-25 13:03
 **/
public class RoleManagerServiceImpl implements RoleManagerService {
    private UserManageDAO umd = new UserManageDAOImpl();
    private RoleManagerDao rmd = new RoleManagerDaoImpl();

    @Override
    public String saveRole(Map<String, Object> values) {

        //JSONObject jsonStr = new JSONObject();
        String roleId = Tools.toString(values.get("ROLEID"));
        if ("".equals(roleId)) {//不存在roleID新增
            roleId = Tools.newId();
            values.put("ROLEID", roleId);
        }
        if (exists(roleId)) {//存在 修改
            return umd.updateRoleInfo(values);
        } else {//不存在新增
            return umd.addRole(values);
        }
    }

    @Override
    public String delRole(String roleId) throws SQLException {
        return umd.delRole(roleId);
    }

    @Override
    public Map<String, Object> getRoleInfo(String roleId) {
        return umd.getRoleInfo(roleId);
    }

    @Override
    public List<Map<String, Object>> findAllRole(int pageIndex, int pageSize) {
        return rmd.findAllRole(pageIndex, pageSize);
    }

    @Override
    public int countRole() {
        return rmd.countRole();
    }

    @Override
    public JSONObject roleExists(String roleName) {
        JSONObject jsonSend = new JSONObject();
        if (umd.roleExists(roleName) == 0) {//存在角色名
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "角色名存在");
        } else {
            jsonSend.put("code", 0);
        }
        return jsonSend;
    }

    @Override
    public List<Map<String, Object>> getMenuInfo(String menuType) {
        if ("-1".equals(menuType)) {//-1显示全部菜单
            return umd.getMenuInfo();
        } else {
            return umd.getMenuInfo(menuType);
        }
    }

    @Override
    public List<Map<String, Object>> getRoleRight(String roleID) {
        return umd.getRoleRight(roleID);
    }

    @Override
    public List<Map<String, Object>> getRoleRightByMenuType(String roleID, String menuType) {
        return umd.getRoleRightByMenuType(roleID, menuType);
    }


    @Override
    public String resetRoleRight(String roleID, List<Map<String, Object>> roleRights) throws SQLException {
        return umd.resetRoleRight(roleID, roleRights);
    }

    @Override
    public List<Map<String, Object>> getSysDict(String fieldName) {
        return rmd.getSysDict(fieldName);
    }

    @Override
    public List<Map<String, Object>> getDict(String classId, String fieldName) {
        DictManager dm = new DictManager();
        List<Map<String, Object>> list = rmd.FindDIctField(classId, fieldName);
        List<Map<String, Object>> dictList = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            String DICTTABLE = Tools.toString(list.get(0).get("DICTTABLE"));
            String tablename = Tools.toString(list.get(0).get("TABLENAME"));
            switch (DICTTABLE.toUpperCase()) {
                case "USERDICT":
                    dictList = dm.getUserDict(tablename, fieldName);
                    break;
                case "SYSDICT":
                    dictList = getSysDict(fieldName);
                    break;
                default:
                    break;
            }
        }
        return dictList;
    }

    @Override
    public Map<String, Object> findRoleModuleById(String roleId, String moduleId) {
        List<Map<String, Object>> list = rmd.findRoleModuleById(roleId, moduleId);
        Map<String, Object> map = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            map = list.get(0);
        }
        return map;
    }

    @Override
    public int saveRoleModule(String roleId, String moduleId, String security, String security2) {
        List<Map<String, Object>> roleModule = rmd.findRoleModuleById(roleId, moduleId);
        if (roleModule.size() > 0) {//存在修改
            return rmd.updateRoleModule(roleId, moduleId, security, security2);
        } else {//不存在新增
            return rmd.addRoleModule(roleId, moduleId, security, security2);
        }
    }

    @Override
    public int roleAssignment(String roleId, List<Map<String, Object>> userMember) throws SQLException {
        return rmd.roleAssignment(roleId, userMember);
    }

    @Override
    public List<Map<String, Object>> roleBoundUsersList(String roleId) {
        return rmd.roleBoundUsersList(roleId);
    }

    @Override
    public List<Map<String, Object>> roleCanBoundUsers(String roleId) {
        return rmd.roleCanBoundUsers(roleId);
    }

    protected Boolean exists(String keyValue) {
        Boolean found = false;
        String sql = "SELECT COUNT(*) as count FROM roleinfo WHERE roleid = '" + keyValue + "'";
        //System.err.println(sql);
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list.size() > 0) {
            String count = list.get(0).get("count").toString();
            int a = Integer.parseInt(count);
            found = a > 0;
            //found = ((Integer) list.get(0).get("count")) > 0;
        }
        return found;
    }


}
