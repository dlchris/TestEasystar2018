package com.tskj.user.userRightService;

import com.sun.org.glassfish.gmbal.Description;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface UserManageService {

    /**
     * @param
     * @return
     * @Description: 获取用户可操作门类
     * @author Mao
     * @date 2019/2/13 15:18
     */
    List<Map<String, Object>> getClassTree(String userID, String roleID);

    /**
     * @param
     * @return
     * @Description: 获取用户可操作功能
     * @author Mao
     * @date 2019/2/13 15:20
     */
    List<Map<String, Object>> getMainMenu(String userID, String roleID, String menuType);

    /**
     * @param
     * @return
     * @Description: 验证用户是否有该权限
     * @author Mao
     * @date 2019/2/13 15:33
     */
    String checkUserRight(String userID, String menuID);

    /**
     * @param
     * @return
     * @Description: 获取用户操作权限
     * @author Mao
     * @date 2019/2/14 11:31
     */
    List<Map<String, Object>> getOperationRight(String userID, String roleID, String menuType, String mainMenu);

    /**
     * @param
     * @return
     * @Description: 获取文件级、盒级、案卷级管理菜单
     * @author Mao
     * @date 2019/2/15 9:55
     */
    Map<String, String> getMenuData(String userID, String roleID);

    /**
     * @param
     * @return
     * @Description: 获取文件级、盒级、案卷级管理菜单
     * @author Mao
     * @date 2019/2/14 15:09
     */
    String getDocMenu(String userID, String roleID);

    /**
     * @param
     * @return
     * @Description: 获取盒级管理菜单
     * @author Mao
     * @date 2019/2/15 9:56
     */
    String getBoxMenu(String userID, String roleID);

    /**
     * @param
     * @return
     * @Description: 获取案卷级管理菜单
     * @author Mao
     * @date 2019/2/15 9:57
     */
    String getRoolMenu(String userID, String roleID);

    /**
     * @param
     * @return
     * @Description: 获取原件管理权限
     * @author Mao
     * @date 2019/2/15 9:20
     */
    String getFileMenu(String userID, String roleID);

    /**
     * @param
     * @return
     * @Description: 获取日志管理权限
     * @author Mao
     * @date 2019/2/15 9:21
     */
    String getLogMenu(String userID, String roleID);

    /**
     * @param userName 用户名    0:存在; 1:不存在
     * @Description: 查询用户名是否存在
     * @author JRX
     * @date 2019/1/28 15:38
     */
    String userExists(String userName);

    /**
     * @param
     * @return 0:成功； 1：失败； 2：用户名已存在
     * @Description: 添加用户，包含用户名验证
     * @author JRX
     * @date 2019/1/28 15:51
     */
    String addUser(Map<String, Object> userInfo);

    /**
     * @param userID 用户ID
     * @Description: 删除用户
     * @author JRX
     * @date 2019/1/28 15:47
     */
    String delUser(String userID) throws SQLException;

    /**
     * @param userID 用户ID， userInfo 用户信息
     * @Description: 修改用户信息
     * @author JRX
     * @date 2019/1/28 15:54
     */
    String updateUserInfo(String userID, Map<String, Object> userInfo) throws SQLException;

    /**
     * @param userId
     * @return
     * @Author JRX
     * @Description:
     * @create 2019/6/9 21:28
     **/
    boolean userCanDel(String userId);

    /**
     * @param pageIndex
     * @param pageSize
     * @return
     * @Author JRX
     * @Description:
     * @create 2019/5/22 15:42
     **/
    List<Map<String, Object>> findAllUser(int pageIndex, int pageSize);

    int countUser();

    //获取用户选中的菜单功能
    List<Map<String, Object>> getUserRightByMenuType(String userId, String menuType);

    //角色绑定菜单功能按钮
    String resetUserRight(String userID, List<Map<String, Object>> userRights) throws SQLException;

    //获取所有角色列表
    List<Map<String, Object>> getRoleList();

    //用户绑定的角色
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

    int saveUserModule(String userId, String moduleId, String security, String security2) throws SQLException;

}
