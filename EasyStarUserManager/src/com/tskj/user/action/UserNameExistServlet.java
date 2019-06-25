package com.tskj.user.action;

import com.alibaba.fastjson.JSONObject;
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

/**
 * @notes: 用户名是否存在
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-06-06 14:49
 **/
@WebServlet(name = "UserNameExistServlet", urlPatterns = "/UserNameExist.do")
public class UserNameExistServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = null;
        JSONObject jsonSend = new JSONObject();
        String Result = "";
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject JsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String userName = Tools.toString(JsonGet.get("userName"));
        if ("".equals(userName)) {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "用户名不能为空");
            Result = jsonSend.toJSONString();
        } else {
            UserManageService ums = new UserManageServiceImpl();
            Result = ums.userExists(userName);
        }
        Tools.sendResponseText(response, Result);
        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "用户名检测", null, Result);

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
