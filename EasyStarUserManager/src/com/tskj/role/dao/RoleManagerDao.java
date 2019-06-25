package com.tskj.role.dao;

import com.sun.istack.internal.NotNull;
import com.tskj.core.db.DbUtility;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @notes: 角色管理  CRUD
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-25 09:43
 **/
public interface RoleManagerDao {

    //查询所有角色并分页
    List<Map<String, Object>> findAllRole(int pageIndex, int pageSize);

    //角色总数
    int countRole();

    //檢查角色名是否存在
    List<Map<String, Object>> findRoleNameUnique(String roleName);

    //添加角色
    int addRole(Map<String, Object> params);

    //修改角色
    int updateRole(Map<String, Object> param, String roleId);

    //删除角色
    int deleteRole();

    //角色绑定用户 (角色分配)
    int roleAssignment(String roleId, List<Map<String, Object>> userMember) throws SQLException;

    //角色已绑定的用户有哪些
    List<Map<String, Object>> roleBoundUsersList(String roleId);

    //对应角色能显示的可以绑定的用户有哪些(角色能绑定的用户有哪些)
    List<Map<String, Object>> roleCanBoundUsers(String roleId);

    //通过用户ID 查询绑定的角色个数
    int boundRoleNumByUserID(String userId);


    //角色添加门类权限

    //角色修改门类权限

    //查看密级，和机构
    List<Map<String, Object>> getSysDict(@NotNull String fieldName);

    public List<Map<String, Object>> FindDIctField(String classId, String fieldName);

    //查看对应档案库对应权限
    List<Map<String, Object>> findRoleModuleById(String roleId, String moduleId);

    //新增角色门类权限
    int addRoleModule(String roleId, String moduleId, String security, String security2);

    //修改角色门类权限
    int updateRoleModule(String roleId, String moduleId, String security, String security2);
}
