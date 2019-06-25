package com.tskj.role.biz;

import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-25 13:01
 **/
public interface RoleManagerService {

    //保存角色或修改角色
    String saveRole(Map<String, Object> values);

    //删除角色
    String delRole(String roleId) throws SQLException;

    //通过角色ID 查询角色信息
    Map<String, Object> getRoleInfo(String roleId);


    //查询所有角色并分页
    List<Map<String, Object>> findAllRole(int pageIndex, int pageSize);

    //角色总数
    int countRole();

    //角色名是否存在
    JSONObject roleExists(String roleName);

    //根据menuType判断来查询菜单
    List<Map<String, Object>> getMenuInfo(String menuType);

    //通过角色ID获取对应角色的菜单权限
    List<Map<String, Object>> getRoleRight(String roleID);

    //获取角色选中的菜单功能
    List<Map<String, Object>> getRoleRightByMenuType(String roleID, String menuType);

    /**
     * @param roleID
     * @param roleRights
     * @return
     * @Author JRX
     * @Description: 设定/重新设定角色权限
     * @create 2019/4/27 17:05
     **/
    String resetRoleRight(String roleID, List<Map<String, Object>> roleRights) throws SQLException;

    //查看密级，和机构
    public List<Map<String, Object>> getSysDict(@NotNull String fieldName);

    //获取档案库对应的机构
    public List<Map<String, Object>> getDict(String classId, @NotNull String fieldName);

    //查看对应档案库对应权限
    Map<String, Object> findRoleModuleById(String roleId, String moduleId);

    int saveRoleModule(String roleId, String moduleId, String security, String security2);

    //角色绑定用户 (角色分配)
    int roleAssignment(String roleId, List<Map<String, Object>> userMember) throws SQLException;


    //角色已绑定的用户有哪些
    List<Map<String, Object>> roleBoundUsersList(String roleId);

    //对应角色能显示的可以绑定的用户有哪些(角色能绑定的用户有哪些)
    List<Map<String, Object>> roleCanBoundUsers(String roleId);

}
