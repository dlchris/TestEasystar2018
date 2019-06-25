package com.tskj.user.userRightServiceImpl;

import com.alibaba.fastjson.JSON;
import com.tskj.core.db.DbConnection;
import com.tskj.user.userRightDAO.UserManageDAO;
import com.tskj.user.userRightDAOImpl.UserManageDAOImpl;
import com.tskj.user.userRightService.UserManageService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManageServiceImpl implements UserManageService {
    private UserManageDAO userManage = new UserManageDAOImpl();

    /**
     * @param
     * @return
     * @Description: 获取用户可操作门类
     * @author Mao
     * @date 2019/2/13 15:20
     */
    @Override
    public List<Map<String, Object>> getClassTree(String userID, String roleID) {
        List<Map<String, Object>> list = userManage.getClassTree(userID, roleID);
        return list;
    }

    /**
     * @param userID
     * @param menuType
     * @return
     * @Description: 获取权限下可操作父级菜单
     * @author Mao
     * @date 2019/2/13 15:20
     */
    @Override
    public List<Map<String, Object>> getMainMenu(String userID, String roleID, String menuType) {
        List<Map<String, Object>> list = userManage.getMenuByType(userID, roleID, menuType);
        return list;
    }

    /**
     * @param userID
     * @param menuID
     * @return
     * @Description: 验证用户是否有该权限
     * @author Mao
     * @date 2019/2/13 15:33
     */
    @Override
    public String checkUserRight(String userID, String menuID) {
        String checkRet = userManage.checkOperationRight(userID, menuID);
        return checkRet;
    }

    /**
     * @param userID
     * @param roleID
     * @param menuType
     * @param mainMenu
     * @return
     * @Description: 获取用户操作权限
     * @author Mao
     * @date 2019/2/14 11:31
     */
    @Override
    public List<Map<String, Object>> getOperationRight(String userID, String roleID, String menuType, String mainMenu) {
        List<Map<String, Object>> list = userManage.getOperationRight(userID, roleID, menuType, mainMenu);
        return list;
    }

    /**
     * @param
     * @return
     * @Description: 获取文件级、盒级、案卷级管理菜单
     * @author Mao
     * @date 2019/2/14 15:09
     */
    @Override
    public Map<String, String> getMenuData(String userID, String roleID) {
        List<Map<String, Object>> list = userManage.getMenuByType(userID, roleID, "4");
//        System.out.println("结果："+list.toString());
        Map<String, String> menuDatas = new HashMap<>();
        String docMenu = "{" +
                "\"entity\":{" +
                "\"id\":1," +
                "\"name\":\"docManage\"," +
                "\"icon\":\"el-icon-document\"," +
                "\"alias\":\"文件级管理\"" +
                "}," +
                "\"childs\":[" +
                "{" +
                "\"entity\":{" +
                "\"id\":11," +
                "\"name\":\"docView\"," +
                "\"icon\":\"el-icon-view\"," +
                "\"alias\":\"查看条目\"" +
                "}" +
                "}";
        String struDoc = "";
        String reportDoc = "";
        String boxMenu = "{" +
                "\"entity\":{" +
                "\"id\":2," +
                "\"name\":\"boxManage\"," +
                "\"icon\":\"el-icon-document\"," +
                "\"alias\":\"盒级管理\"" +
                "}," +
                "\"childs\":[" +
                "{" +
                "\"entity\":{" +
                "\"id\":21," +
                "\"name\":\"boxView\"," +
                "\"icon\":\"el-icon-view\"," +
                "\"alias\":\"查看条目\"" +
                "}" +
                "}";
        String struBox = "";
        String reportBox = "";
        String roolMenu = "{" +
                "\"entity\":{" +
                "\"id\":3," +
                "\"name\":\"roolManage\"," +
                "\"icon\":\"el-icon-document\"," +
                "\"alias\":\"案卷级管理\"" +
                "}," +
                "\"childs\":[" +
                "{" +
                "\"entity\":{" +
                "\"id\":31," +
                "\"name\":\"roolView\"," +
                "\"icon\":\"el-icon-view\"," +
                "\"alias\":\"查看条目\"" +
                "}" +
                "}";
        String struRool = "";
        String reportRool = "";
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                if ("MINFO000000000040104".equals(map.get("MENUID"))) {
                    struDoc = ",{" +
                            "\"entity\":{" +
                            "\"id\":12," +
                            "\"name\":\"docStructSetup\"," +
                            "\"icon\":\"el-icon-setting\"," +
                            "\"alias\":\"结构设置\"" +
                            "}" +
                            "}";
                    struBox = ",{" +
                            "\"entity\":{" +
                            "\"id\":22," +
                            "\"name\":\"boxStructSetup\"," +
                            "\"icon\":\"el-icon-setting\"," +
                            "\"alias\":\"结构设置\"" +
                            "}" +
                            "}";
                    struRool = ",{" +
                            "\"entity\":{" +
                            "\"id\":32," +
                            "\"name\":\"roolStructSetup\"," +
                            "\"icon\":\"el-icon-setting\"," +
                            "\"alias\":\"结构设置\"" +
                            "}" +
                            "}";
                }
                if ("MINFO000000000040102".equals(map.get("MENUID"))) {
                    reportDoc = ",{" +
                            "\"entity\":{" +
                            "\"id\":13," +
                            "\"name\":\"docReportSetup\"," +
                            "\"icon\":\"el-icon-printer\"," +
                            "\"alias\":\"报表设置\"" +
                            "}" +
                            "}";
                    reportBox = ",{" +
                            "\"entity\":{" +
                            "\"id\":23," +
                            "\"name\":\"boxReportSetup\"," +
                            "\"icon\":\"el-icon-printer\"," +
                            "\"alias\":\"报表设置\"" +
                            "}" +
                            "}";
                    reportRool = ",{" +
                            "\"entity\":{" +
                            "\"id\":33," +
                            "\"name\":\"roolReportSetup\"," +
                            "\"icon\":\"el-icon-printer\"," +
                            "\"alias\":\"报表设置\"" +
                            "}" +
                            "}";
                }
            }
        }
        /*docMenu = docMenu + struDoc + reportDoc + "]}";
        boxMenu = boxMenu + struBox + reportBox + "]}";
        roolMenu = roolMenu + struRool + reportRool + "]}";*/

        docMenu = docMenu + "]}";
        boxMenu = boxMenu + "]}";
        roolMenu = roolMenu + "]}";
        menuDatas.put("docMenu", docMenu);
        menuDatas.put("boxMenu", boxMenu);
        menuDatas.put("roolMenu", roolMenu);

        return menuDatas;
    }

    /**
     * @param
     * @return
     * @Description: 获取文件级管理菜单
     * @author Mao
     * @date 2019/2/14 15:09
     */
    @Override
    public String getDocMenu(String userID, String roleID) {
        List<Map<String, Object>> list = userManage.getMenuByType(userID, roleID, "4");
        String docMenu = "{" +
                "\"entity\":{" +
                "\"id\":1," +
                "\"name\":\"docManage\"," +
                "\"icon\":\"el-icon-document\"," +
                "\"alias\":\"文件级管理\"" +
                "}," +
                "\"childs\":[" +
                "{" +
                "\"entity\":{" +
                "\"id\":11," +
                "\"name\":\"docView\"," +
                "\"icon\":\"el-icon-view\"," +
                "\"alias\":\"查看条目\"" +
                "}" +
                "}";
        String struDoc = "";
        String reportDoc = "";
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                if ("MINFO000000000040104".equals(map.get("MENUID"))) {
                    struDoc = ",{" +
                            "\"entity\":{" +
                            "\"id\":12," +
                            "\"name\":\"docStructSetup\"," +
                            "\"icon\":\"el-icon-setting\"," +
                            "\"alias\":\"结构设置\"" +
                            "}" +
                            "}";
                }
                if ("MINFO000000000040102".equals(map.get("MENUID"))) {
                    reportDoc = ",{" +
                            "\"entity\":{" +
                            "\"id\":13," +
                            "\"name\":\"docReportSetup\"," +
                            "\"icon\":\"el-icon-printer\"," +
                            "\"alias\":\"报表设置\"" +
                            "}" +
                            "}";
                }
            }
        }
//        docMenu = docMenu + struDoc + reportDoc + "]}";
        docMenu = docMenu + "]}";

        return docMenu;
    }

    /**
     * @param
     * @return
     * @Description: 获取盒级管理菜单
     * @author Mao
     * @date 2019/2/15 9:56
     */
    @Override
    public String getBoxMenu(String userID, String roleID) {
        List<Map<String, Object>> list = userManage.getMenuByType(userID, roleID, "4");
        String boxMenu = "{" +
                "\"entity\":{" +
                "\"id\":2," +
                "\"name\":\"boxManage\"," +
                "\"icon\":\"el-icon-document\"," +
                "\"alias\":\"盒级管理\"" +
                "}," +
                "\"childs\":[" +
                "{" +
                "\"entity\":{" +
                "\"id\":21," +
                "\"name\":\"boxView\"," +
                "\"icon\":\"el-icon-view\"," +
                "\"alias\":\"查看条目\"" +
                "}" +
                "}";
        String struBox = "";
        String reportBox = "";
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                if ("MINFO000000000040104".equals(map.get("MENUID"))) {
                    struBox = ",{" +
                            "\"entity\":{" +
                            "\"id\":22," +
                            "\"name\":\"boxStructSetup\"," +
                            "\"icon\":\"el-icon-setting\"," +
                            "\"alias\":\"结构设置\"" +
                            "}" +
                            "}";
                }
                if ("MINFO000000000040102".equals(map.get("MENUID"))) {
                    reportBox = ",{" +
                            "\"entity\":{" +
                            "\"id\":23," +
                            "\"name\":\"boxReportSetup\"," +
                            "\"icon\":\"el-icon-printer\"," +
                            "\"alias\":\"报表设置\"" +
                            "}" +
                            "}";
                }
            }
        }
//        boxMenu = boxMenu + struBox + reportBox + "]}";
        boxMenu = boxMenu + "]}";
        return boxMenu;
    }

    /**
     * @param
     * @return
     * @Description: 获取案卷级管理菜单
     * @author Mao
     * @date 2019/2/15 9:57
     */
    @Override
    public String getRoolMenu(String userID, String roleID) {
        List<Map<String, Object>> list = userManage.getMenuByType(userID, roleID, "4");
        String roolMenu = "{" +
                "\"entity\":{" +
                "\"id\":3," +
                "\"name\":\"roolManage\"," +
                "\"icon\":\"el-icon-document\"," +
                "\"alias\":\"案卷级管理\"" +
                "}," +
                "\"childs\":[" +
                "{" +
                "\"entity\":{" +
                "\"id\":31," +
                "\"name\":\"roolView\"," +
                "\"icon\":\"el-icon-view\"," +
                "\"alias\":\"查看条目\"" +
                "}" +
                "}";
        String struRool = "";
        String reportRool = "";
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                if ("MINFO000000000040104".equals(map.get("MENUID"))) {
                    struRool = ",{" +
                            "\"entity\":{" +
                            "\"id\":32," +
                            "\"name\":\"roolStructSetup\"," +
                            "\"icon\":\"el-icon-setting\"," +
                            "\"alias\":\"结构设置\"" +
                            "}" +
                            "}";
                }
                if ("MINFO000000000040102".equals(map.get("MENUID"))) {
                    reportRool = ",{" +
                            "\"entity\":{" +
                            "\"id\":33," +
                            "\"name\":\"roolReportSetup\"," +
                            "\"icon\":\"el-icon-printer\"," +
                            "\"alias\":\"报表设置\"" +
                            "}" +
                            "}";
                }
            }
        }
        roolMenu = roolMenu + struRool + reportRool + "]}";
        return roolMenu;
    }

    /**
     * @param
     * @return
     * @Description: 获取原件管理权限
     * @author Mao
     * @date 2019/2/15 9:20
     */
    @Override
    public String getFileMenu(String userID, String roleID) {
        List<Map<String, Object>> list = userManage.getAppointRight(userID, roleID, "MINFO000000000020115");
        if (list != null && !list.isEmpty()) {
            return "{" +
                    "\"entity\":{" +
                    "\"id\":4," +
                    "\"name\":\"fileManage\"," +
                    "\"icon\":\"el-icon-document\\r\\n\"," +
                    "\"alias\":\"原件管理\"" +
                    "}" +
                    "}";
        }
        return "";
    }

    /**
     * @param
     * @return
     * @Description: 获取日志管理权限
     * @author Mao
     * @date 2019/2/15 9:21
     */
    @Override
    public String getLogMenu(String userID, String roleID) {
        List<Map<String, Object>> list = userManage.getAppointRight(userID, roleID, "MINFO000000000040103");
        if (list != null && !list.isEmpty()) {
            return "{" +
                    "\"entity\":{" +
                    "\"id\":5," +
                    "\"name\":\"logManage\"," +
                    "\"icon\":\"el-icon-message\\r\\n\"," +
                    "\"alias\":\"日志管理\"" +
                    "}" +
                    "}";
        }
        return "";
    }

    @Override
    public String userExists(String userName) {
        return userManage.userExists(userName);
    }

    @Override
    public String addUser(Map<String, Object> userInfo) {
        return userManage.addUser(userInfo);
    }

    @Override
    public String delUser(String userID) throws SQLException {
        return userManage.delUser(userID);
    }

    @Override
    public String updateUserInfo(String userID, Map<String, Object> userInfo) throws SQLException {
        return userManage.updateUserInfo(userID, userInfo);
    }

    @Override
    public boolean userCanDel(String userId) {
        return userManage.userCanDel(userId);
    }

    @Override
    public List<Map<String, Object>> findAllUser(int pageIndex, int pageSize) {
        return userManage.findAllUser(pageIndex, pageSize);
    }

    @Override
    public int countUser() {
        return userManage.countUser();
    }

    @Override
    public List<Map<String, Object>> getUserRightByMenuType(String userId, String menuType) {
        return userManage.getUserRightByMenuType(userId, menuType);
    }

    @Override
    public String resetUserRight(String userID, List<Map<String, Object>> userRights) throws SQLException {
        return userManage.resetUserRight(userID, userRights);
    }

    @Override
    public List<Map<String, Object>> getRoleList() {

        return userManage.getRoleList();
    }

    @Override
    public List<Map<String, Object>> getUserBoundRoleByUserId(String userId) {
        return userManage.getUserBoundRoleByUserId(userId);
    }

    @Override
    public int saveUserBoundRole(String userId, List<Map<String, Object>> userMember) throws SQLException {
        return userManage.saveUserBoundRole(userId, userMember);
    }

    @Override
    public List<Map<String, Object>> userCTreePower(String classId, String userId) {
        return userManage.userCTreePower(classId, userId);
    }

    @Override
    public int saveUserModule(String userId, String moduleId, String security, String security2) throws SQLException {
        Connection conn = DbConnection.getConnection();
        conn.setAutoCommit(false);
        int num = -1;
        try {
            List<Map<String, Object>> userModule = userManage.findUserModuleById(conn, userId, moduleId);
            if (userModule.size() > 0) {//存在修改
                num = userManage.updateUserModule(conn, userId, moduleId, security, security2);

            } else {//不存在新增
                num = userManage.addUserModule(conn, userId, moduleId, security, security2);
            }
            if (num != 0) {
                conn.rollback();
            } else {
                conn.commit();
            }
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
        return num;
    }
}
