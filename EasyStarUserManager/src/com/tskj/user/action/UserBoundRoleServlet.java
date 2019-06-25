package com.tskj.user.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.role.biz.RoleManagerService;
import com.tskj.role.biz.impl.RoleManagerServiceImpl;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.user.userRightService.UserManageService;
import com.tskj.user.userRightServiceImpl.UserManageServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-05-23 15:42
 **/
@WebServlet(name = "UserBoundRoleServlet", urlPatterns = "/UserBoundRole.do")
public class UserBoundRoleServlet extends HttpServlet {
    private RoleManagerService rms = new RoleManagerServiceImpl();
    private UserManageService ums = new UserManageServiceImpl();

    public SessionDataBiz getpermanent(HttpServletRequest request) {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionDataImpl;
    }

    //用户绑定角色
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserManageService ums = new UserManageServiceImpl();
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String userId = jsonGet.getString("userId");
        try {
            JSONArray userBoundRoles = jsonGet.getJSONArray("UserBoundRoles");
            List<Map<String, Object>> lists = JSONArray.parseObject(userBoundRoles.toString(), List.class);
            //List<Map<String, Object>> list = new ArrayList<>();
            //用户可以绑定的角色个数
            ResourceBundle resourceBundle = ResourceBundle.getBundle("/config/db/systemSetup");
            String unum = resourceBundle.getString("UserBindRolesNum");
            Integer num = 0;
            num = Integer.valueOf(unum);
            System.err.println("绑定个数:" + userBoundRoles.size() + "个");

            if (userBoundRoles != null && userBoundRoles.size() > 0 && userBoundRoles.size() <= num) {

                System.err.println(lists);
                //保存用户绑定角色权限
                int rel = ums.saveUserBoundRole(userId, lists);
                if (rel == 0) {
                    jsonSend.put("code", 0);
                } else {
                    jsonSend.put("code", 1);
                    jsonSend.put("errMsg", "保存失败");
                }
            } else {
                jsonSend.put("code", 1);
                jsonSend.put("errMsg", "用户绑定角色是否超过规定限制数量或数据传输是否有问题");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "解析出现问题!!!");

        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "用户绑定角色", null, jsonSend.toString());

    }

    //角色列表+用户选中的角色
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();
        String userId = request.getParameter("userId");
        if (userId != null && !userId.isEmpty()) {
            //角色列表
            List<Map<String, Object>> roleList = ums.getRoleList();
            //用户选中的角色集合
            List<Map<String, Object>> userBoundRoleList = ums.getUserBoundRoleByUserId(userId);
            //用户可以绑定的角色个数
            ResourceBundle resourceBundle = ResourceBundle.getBundle("/config/db/systemSetup");
            String ubrn = resourceBundle.getString("UserBindRolesNum");
            Integer num = 0;
            try {
                num = Integer.valueOf(ubrn);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                jsonSend.put("code", 1);
                jsonSend.put("errMsg", "sysetemSetup配置文件读取UserBindRolesNum值不是整数");
                Tools.sendResponseText(response, jsonSend.toString());
                return;
            }
            jsonSend.put("code", 0);
            jsonSend.put("roleList", roleList);
            jsonSend.put("userBoundRole", userBoundRoleList);
            jsonSend.put("canBoundNum", num);
        } else {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "数据不能为空!!!");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "角色列表+用户选中的角色", null, jsonSend.toString());
    }
}
