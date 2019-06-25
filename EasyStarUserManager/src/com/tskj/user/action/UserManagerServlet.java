package com.tskj.user.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-05-21 16:33
 **/
@WebServlet(name = "UserManagerServlet", urlPatterns = "/UserManager.do")
public class UserManagerServlet extends HttpServlet {
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


    //保存用户信息
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.err.println("保存用户!!!");
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        JSONObject value = jsonGet.getJSONObject("data");
        //增加主键UUID
        value.put("USERID", Tools.newId());
        if (value != null && !value.isEmpty()) {
            String resultMsg = ums.addUser(value);
            Tools.sendResponseText(response, resultMsg);
        } else {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "保存数据不能为空");
            Tools.sendResponseText(response, jsonSend.toString());
        }

        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "保存用户信息", null, jsonSend.toString());
    }

    //获取用户列表 分页
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonSend = new JSONObject();
        SessionDataBiz sessionDataImpl = getpermanent(request);
        try {
            int pageSize = Integer.valueOf(request.getParameter("pageSize"));
            int pageIndex = Integer.valueOf(request.getParameter("pageIndex"));
            List<Map<String, Object>> allUser = ums.findAllUser(pageIndex, pageSize);
            int size = ums.countUser();
            jsonSend.put("code", 0);
            jsonSend.put("list", allUser);
            jsonSend.put("count", size);
        } catch (Exception e) {
            e.printStackTrace();
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", e.getMessage());
        }
        Tools.sendResponseText(response, JSON.toJSONString(jsonSend, SerializerFeature.WriteMapNullValue));
        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "获取用户列表 分页", null, null);
    }

    //修改用户信息
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonSend = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(req));
        SessionDataBiz sessionDataImpl = getpermanent(req);
        String userId = jsonGet.getString("userId");
        JSONObject value = jsonGet.getJSONObject("data");
        if (value != null && !value.isEmpty() && userId != null && !userId.isEmpty()) {
            String resultMsg = null;
            try {
                resultMsg = ums.updateUserInfo(userId, value);
            } catch (SQLException e) {
                e.printStackTrace();
                jsonSend.put("code", 1);
                jsonSend.put("errMsg", "修改用户出错");
                resultMsg = jsonSend.toString();
            }
            Tools.sendResponseText(resp, resultMsg);
        } else {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "修改数据不能为空");
            Tools.sendResponseText(resp, jsonSend.toString());
        }
        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "修改用户信息", null, jsonSend.toString());


    }

    //删除用户
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonSend = new JSONObject();
        String Result = "";
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(req));
        SessionDataBiz sessionDataImpl = getpermanent(req);
        String userId = jsonGet.getString("userId");
        if (userId != null && !"".equals(userId)) {
            try {
                //判断用户是否能删除
                if (ums.userCanDel(userId)) {
                    Result = ums.delUser(userId);
                    //Tools.sendResponseText(resp, relsutMsg);
                } else {
                    jsonSend.put("code", 1);
                    jsonSend.put("errMsg", "用户不能被删除");
                    Result = jsonSend.toString();
                }
                //return;
            } catch (SQLException e) {
                e.printStackTrace();
                jsonSend.put("code", 1);
                jsonSend.put("errMsg", "删除用户失败");
                Result = jsonSend.toString();
            }
        } else {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "传参有误,检测传值是否正确");
            Result = jsonSend.toString();
        }
        Tools.sendResponseText(resp, Result);
        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "删除用户", null, Result);
    }
}
