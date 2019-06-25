package com.tskj.role.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.role.biz.RoleManagerService;
import com.tskj.role.biz.impl.RoleManagerServiceImpl;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-27 22:19
 **/
@WebServlet(name = "RoleBoundUsersServlet", urlPatterns = "/RoleBoundUsers.do")
public class RoleBoundUsersServlet extends HttpServlet {
    private RoleManagerService rms = new RoleManagerServiceImpl();

    public SessionDataBiz getpermanent(HttpServletRequest request) {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionDataImpl;
    }

    //角色绑定用户
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String roleId = jsonGet.getString("ROLEID");
        if ("".equals(roleId)) {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "角色ID不能为空");
        }
        JSONArray datas = jsonGet.getJSONArray("datas");
        List<Map<String, Object>> lists = JSONArray.parseObject(datas.toString(), List.class);
        if (!lists.isEmpty()) {
            if (!lists.get(0).containsKey("USERID") || !lists.get(0).containsKey("ROLEID")) {
                jsonSend.put("code", 1);
                jsonSend.put("errMsg", "datas集合传参有误");
            }
        }
        try {
            int num = rms.roleAssignment(roleId, lists);
            if (num == 0) {
                jsonSend.put("code", 0);
            } else {
                jsonSend.put("code", 1);
                jsonSend.put("errMsg", "保存失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "保存失败");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "角色绑定用户", null, jsonSend.toString());
    }

    //查看所有用户,并且查看对应存在该权限的用户
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();
        try {
            //SessionDataBiz sessionDataBiz = PermanentDataSourceFactory.getSessionDataImpl(request);
            //String roleId = sessionDataBiz.getUserInfo().getRoleId();
            String roleId = request.getParameter("ROLEID");
            if ("".equals(roleId)) {
                jsonSend.put("code", 1);
                jsonSend.put("errMsg", "角色ID不能为空");
            }
            //角色已绑定的用户
            List<Map<String, Object>> list = rms.roleBoundUsersList(roleId);
            //角色能绑定的用户
            List<Map<String, Object>> mapList = rms.roleCanBoundUsers(roleId);
            jsonSend.put("code", 0);
            jsonSend.put("roleBoundUsers", list);
            jsonSend.put("roleCanBoundUsers", mapList);
        } catch (Exception e) {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "查询出现错误");
            e.printStackTrace();
        } finally {
            Tools.sendResponseText(response, jsonSend.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "查看所有用户,并且查看对应存在该权限的用户"
                    , null, jsonSend.toString());

        }


    }
}
