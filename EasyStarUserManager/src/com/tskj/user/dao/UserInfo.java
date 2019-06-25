package com.tskj.user.dao;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.log.LogUtils;
import com.sun.istack.internal.NotNull;
import com.tskj.core.db.DbUtility;
import com.tskj.core.system.consts.UserBindRolesNum;
import com.tskj.core.system.utility.Tools;
import com.tskj.user.biz.UserInfoBiz;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 用户信息类
 *
 * @author LeonSu
 */
public class UserInfo implements UserInfoBiz {

    public void UserInfo() {
        isEncrypt = false;
    }

    private String userId = "";
    private String roleId = "";
    private String userName = "";
    private String password = "";
    private String prop = "";//三员分立 0.档案员(保密员)1.系统管理员(系统设置)2.审计员(日志管理员)

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEncrypt() {
        return isEncrypt;
    }

    public void setEncrypt(boolean encrypt) {
        isEncrypt = encrypt;
    }

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }

    private boolean isEncrypt;

    @Override
    public JSONObject login(@NotNull String userName, @NotNull String password) {
        String sql = "SELECT * FROM REGUSERINFO WHERE USERNAME='" + userName + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        JSONObject jsonRet = JSONObject.parseObject("{}");

        if (list == null || list.size() == 0) {
            jsonRet.put("code", 1);
            jsonRet.put("errMsg", "无此用户");
            return jsonRet;
        }
        Object userPwd = list.get(0).get("USERPW");
        if (userPwd == null) {
            jsonRet.put("code", 1);
            jsonRet.put("errMsg", "密码不正确");
            return jsonRet;
        }

        if (!userPwd.toString().trim().equalsIgnoreCase(password)) {
            jsonRet.put("code", 1);
            jsonRet.put("errMsg", "密码不正确");
            return jsonRet;
        }
        userId = list.get(0).get("USERID").toString().trim();
        userName = list.get(0).get("USERNAME").toString().trim();
        prop = Tools.toString(list.get(0).get("PROP")).trim();
        //查角色ID  //查询语句有问题 应该是查 USERMEMBER 通过userid
        //sql = "SELECT ROLEID FROM ROLEINFO WHERE ROLENAME='" + userName + "'";
        sql = "SELECT ROLEID FROM USERMEMBER WHERE USERID = '" + userId + "'";
        ResourceBundle resourceBundle = ResourceBundle.getBundle("/config/db/systemSetup");
        String ubrn = resourceBundle.getString("UserBindRolesNum");
        Integer num;
        try {
            num = Integer.valueOf(ubrn);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            jsonRet.put("code", 1);
            jsonRet.put("errMsg", "sysetemSetup配置文件读取UserBindRolesNum值不是整数");
            return jsonRet;
        }
        List<Map<String, Object>> list2 = DbUtility.execSQL(sql);
        String roleId = "";
        if (list2 != null && !list2.isEmpty()) {
            if (num == 1) {//只有一个
                roleId = list2.get(0).get("ROLEID").toString();
            } else {
                //后续开放绑定多个角色在增加代码
                System.err.println("后续开放绑定多个角色在增加代码");
            }
        }
        this.userName = userName;
        this.roleId = roleId;
        this.password = password;
        this.prop = prop;
        System.err.println("用户身份:" + prop);
        jsonRet.put("code", 0);
        jsonRet.put("errMsg", "");
        jsonRet.put("prop", prop);
        return jsonRet;
    }

    @Override
    public JSONObject setNewPassword(String oldPwd, String newPwd) {
        return null;
    }

    @Override
    public boolean userExists(String userName) {
        return true;
    }
}
