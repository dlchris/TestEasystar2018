package com.easystar.action.login;

import com.alibaba.fastjson.JSONObject;
import com.easystar.system.utility.CookieTools;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.user.dao.UserInfo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户登录action
 *
 * @author LeonSu
 */
@WebServlet(name = "LoginAction", urlPatterns = "/Login.do")
public class LoginAction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String callback = request.getParameter("callback");

        String originHeader = request.getHeader("Origin");
        UserInfo userInfo = new UserInfo();
        JSONObject jsonRet = JSONObject.parseObject("{}");
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        if (!jsonGet.containsKey("userName")) {
            jsonRet.put("code", 1);
            jsonRet.put("errMsg", "用户名不能为空");
        }
        String userName = jsonGet.getString("userName");
        String password = "";
        if (jsonGet.containsKey("password")) {
            password = jsonGet.getString("password");
        }
        jsonRet = userInfo.login(userName, password);

        //登录成功，保存用户ID到session中
        if (jsonRet.getInteger("code") == 0) {

            HttpSession session = request.getSession();
            if (session.getAttribute("userInfo") != null) {
                session.invalidate();
                session = request.getSession();
            }
            session.setAttribute("userInfo", userInfo);
            session.setAttribute("perType", "SESSION");

            Cookie cookie = CookieTools.getCookie(request.getCookies(), "JSESSIONID");
            if (cookie == null) {
                cookie = new Cookie("JSESSIONID", session.getId());
            } else {
                cookie.setValue(session.getId());
            }
            cookie.setPath("/");
            cookie.setMaxAge(-1);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
        if (null != callback && !callback.isEmpty()) {
            Tools.sendResponseText(response, originHeader, callback + "(" + jsonRet.toString() + ")");
        } else {
            Tools.sendResponseText(response, originHeader, jsonRet.toJSONString());
        }

        LogUtil.emptySessionLog(request, logModuleConsts.ZHGL, "登录",
                "用户尝试登录" + userName + "账号", jsonRet.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(404);
    }
}
