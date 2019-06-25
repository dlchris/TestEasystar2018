package com.tskj.user.biz;

import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;

import java.util.List;
import java.util.Map;

/**
 * 用户信息接口
 * @author LeonSu
 */
public interface UserInfoBiz {
    /**
     * 用户登录
     * @param userName
     * @param password
     * @return
     *  JSONObject，<br>code：整型，0：成功，非0：失败 <br>errMsg：字符串，错误原因
     */
    JSONObject login(@NotNull String userName, @NotNull String password);

    /**
     * 修改密码
     * @param oldPwd
     *  旧密码
     * @param newPwd
     *  新密码
     * @return
     */
    JSONObject setNewPassword(String oldPwd, String newPwd);

    /**
     * 根据用户名或用户ID，检查用户是否存在
     * @param userName or userId
     * @return
     *  true：用户存在；false：用户不存在
     */
    boolean userExists(String userName);

    //查看所有用户
    //List<Map<String, Object>> findAllUsers();


}
