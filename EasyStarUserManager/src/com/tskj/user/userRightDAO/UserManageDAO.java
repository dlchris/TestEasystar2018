package com.tskj.user.userRightDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface UserManageDAO {

    //@return 0:成功； 1：失败；

    /**
     * @param pageIndex
     * @param pageSize
     * @return
     * @Author JRX
     * @Description: 用户列表分页
     * @create 2019/5/22 15:33
     **/
    List<Map<String, Object>> findAllUser(int pageIndex, int pageSize);

    /**
     * @return
     * @Author JRX
     * @Description: 用户总数
     * @create 2019/5/22 15:47
     **/
    int countUser();

    /**
     * @param userName 用户名
     * @Description: 根据用户名获取用户信息
     * @author Mao
     * @date 2019/1/28 15:36
     */
    List<Map<String, Object>> getUserInfo(String userName);

    /**
     * @param userName 用户名    0:存在; 1:不存在
     * @Description: 查询用户名是否存在
     * @author Mao
     * @date 2019/1/28 15:38
     */
    String userExists(String userName);

    /**
     * @param
     * @return 0:成功； 1：失败； 2：用户名已存在
     * @Description: 添加用户，包含用户名验证
     * @author Mao
     * @date 2019/1/28 15:51
     */
    String addUser(Map<String, Object> userInfo);

    /**
     * @param userID 用户ID
     * @Description: 删除用户
     * @author Mao
     * @date 2019/1/28 15:47
     */
    String delUser(String userID) throws SQLException;

    boolean userCanDel(String userId);

    /**
     * @param userID 用户ID， userInfo 用户信息
     * @Description: 修改用户信息
     * @author Mao
     * @date 2019/1/28 15:54
     */
    String updateUserInfo(String userID, Map<String, Object> userInfo) throws SQLException;

    //String checkUserTheLast(String userId);


    /*************************************************************************************************************/
    /**
     * @param
     * @return
     * @Description: 验证用户权限
     * @author Mao
     * @date 2019/1/29 15:09
     */
    String checkOperationRight(String userID, String menuID);

    /**
     * @param
     * @return
     * @Description: 获取用户权限
     * @author Mao
     * @date 2019/1/29 15:10
     */
    List<Map<String, Object>> getUserRight(String userID);

    /**
     * @param
     * @return
     * @Description: 根据菜单类型获取权限  用户角色一对多
     * @author Mao
     * @date 2019/2/13 10:55
     */
    List<Map<String, Object>> getUserRightByType(String userID, String menuType);

    /**
     * @param
     * @return
     * @Description: 根据菜单类型获取权限   用户角色一对一
     * @author Mao
     * @date 2019/2/13 15:54
     */
    List<Map<String, Object>> getMenuByType(String userID, String roleID, String menuType);

    /**
     * @param
     * @return
     * @Description: 获取所有菜单信息
     * @author Mao
     * @date 2019/1/29 15:13
     */
    List<Map<String, Object>> getMenuInfo();

    /**
     * @param
     * @return
     * @Description: 根据menuType查询菜单
     * @author Mao
     * @date 2019/1/30 13:54
     */
    List<Map<String, Object>> getMenuInfo(String menuType);

    /**
     * @param
     * @return
     * @Description: 重新设置用户权限，先清除原权限，添加新的权限集合
     * @author Mao
     * @date 2019/1/29 15:18
     */
    String resetUserRight(String userID, List<Map<String, Object>> userRights) throws SQLException;

    /**
     * @param
     * @return
     * @Description: 删除用户某权限
     * @author Mao
     * @date 2019/1/29 15:21
     */
    String delUserRight(String userID, String menuID);

    /**
     * @param
     * @return
     * @Description: 添加用户权限
     * @author Mao
     * @date 2019/1/29 15:23
     */
    String addUserRight(String userID, String menuID);

    /**
     * @param
     * @return
     * @Description: 角色名是否已存在
     * @author Mao
     * @date 2019/1/30 15:52
     */
    int roleExists(String roleName);

    /**
     * @param
     * @return
     * @Description: 添加角色
     * @author Mao
     * @date 2019/1/29 16:00
     */
    String addRole(Map<String, Object> roleInfo);

    /**
     * @param
     * @return
     * @Description: 删除角色
     * @author Mao
     * @date 2019/1/29 16:04
     */
    String delRole(String roleID) throws SQLException;

    /**
     * @param
     * @return
     * @Description: 修改角色信息
     * @author Mao
     * @date 2019/1/29 16:10
     */
    String updateRoleInfo(Map<String, Object> roleInfo);

    /**
     * @param
     * @return
     * @Description: 获取角色信息
     * @author Mao
     * @date 2019/1/29 16:06
     */
    Map<String, Object> getRoleInfo(String roleID);

    /**
     * @param
     * @return
     * @Description: 获取角色集合
     * @author Mao
     * @date 2019/1/29 16:05
     */
    List<Map<String, Object>> getRoleList();

    /**
     * @param
     * @return
     * @Description: 设定/重新设定角色权限
     * @author Mao
     * @date 2019/1/29 16:11
     */
    String resetRoleRight(String roleID, List<Map<String, Object>> roleRights) throws SQLException;

    /**
     * @param
     * @return
     * @Description: 获取角色权限
     * @author Mao
     * @date 2019/1/29 16:14
     */
    List<Map<String, Object>> getRoleRight(String roleID);

    /**
     * @param roleID
     * @param menuType
     * @return
     * @Author JRX
     * @Description:
     * @create 2019/5/21 15:41
     **/
    List<Map<String, Object>> getRoleRightByMenuType(String roleID, String menuType);

    /**
     * @param
     * @return
     * @Description: 获取某角色下用户信息
     * @author Mao
     * @date 2019/1/29 16:15
     */
    List<Map<String, Object>> getUsersByRoleID(String roleID);

    /**
     * @param
     * @return
     * @Description: 验证角色权限
     * @author Mao
     * @date 2019/1/29 16:15
     */
    String checkRoleRight(String roleID, String menuID);

    /**
     * @param
     * @return
     * @Description: 删除角色某权限
     * @author Mao
     * @date 2019/1/29 16:17
     */
    String delRoleRight(String roleID, String menuID);

    /**
     * @param
     * @return
     * @Description: 赋予角色某权限
     * @author Mao
     * @date 2019/1/29 16:18
     */
    String addRoleRight(String roleID, String menuID);

    /**
     * @param
     * @return
     * @Description: 为用户分配/重置角色
     * @author Mao
     * @date 2019/1/29 16:19
     */
    String resetUserRole(String userID, List<Map<String, Object>> userRoles) throws SQLException;

    /**
     * @param
     * @return
     * @Description: 获取用户所属角色信息
     * @author Mao
     * @date 2019/1/29 16:23
     */
    List<Map<String, Object>> getUserRole(String userID);

    /**
     * @param
     * @return
     * @Description: 获取用户可操作门类
     * @author Mao
     * @date 2019/2/13 14:33
     */
    List<Map<String, Object>> getClassTree(String userID, String roleID);


    /**
     * @param
     * @return
     * @Description: 获取用户可操作门类
     * @author Mao
     * @date 2019/2/13 15:16
     */
    List<Map<String, Object>> getClassTree(String userID);

    /**
     * @param
     * @return
     * @Description: 获取功能操作权限
     * @author Mao
     * @date 2019/2/14 10:51
     */
    List<Map<String, Object>> getOperationRight(String userID, String roleID, String menuType, String mainMenu);

    /**
     * @param
     * @return
     * @Description: 获取用户指定权限
     * @author Mao
     * @date 2019/2/15 9:15
     */
    List<Map<String, Object>> getAppointRight(String userID, String roleID, String menuID);


    int changePassWord(String userId, String newPass);

    List<Map<String, Object>> getUserRightByMenuType(String userId, String menuType);

    /**
     * @param userId
     * @return
     * @Author JRX
     * @Description: 用户绑定的角色
     * @create 2019/5/23 16:03
     **/
    List<Map<String, Object>> getUserBoundRoleByUserId(String userId);

    //保存用户绑定角色 (用户角色分配)
    int saveUserBoundRole(String userId, List<Map<String, Object>> userMember) throws SQLException;

    /**
     * @param classId
     * @param userId
     * @return
     * @Author JRX
     * @Description: 用户对应门类权限
     * @create 2019/5/24 10:15
     **/
    List<Map<String, Object>> userCTreePower(String classId, String userId);


    //获取字段类型
    Map<String, Object> getFieldType(String classId, String fieldName);

    //获取系统字典字段信息
    List<Map<String, Object>> getSysDictFieldList(String fieldName);

    //获取用户字典字段信息
    List<Map<String, Object>> getUserDictFieldList(String classId, String fieldName);

    //判断权限字段是系统字段还是用户字段
    //List<Map<String, Object>> FindDIctField(String classId, String fieldName);
    //查看对应档案库对应权限
    List<Map<String, Object>> findUserModuleById(Connection conn, String userId, String moduleId);

    //新增用户门类权限
    int addUserModule(Connection conn,String userId, String moduleId, String security, String security2);

    //修改用户门类权限
    int updateUserModule(Connection conn,String userId, String moduleId, String security, String security2);
}
