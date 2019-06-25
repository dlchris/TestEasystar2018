package com.easystar.action.login;

import com.alibaba.fastjson.JSONObject;
import com.tskj.fileimport.system.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "LogoutAction", urlPatterns = "/Logout.do")
public class LogoutAction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = null;
        JSONObject json = JSONObject.parseObject("{}");
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
            //清除session
            PermanentDataSourceFactory.removeSessionDataImpl(request);

            json.put("code", 0);
            json.put("errMsg", "");
            response.setStatus(200);
        } catch (Exception e) {
            e.printStackTrace();
            //LogUtil.emptySErrorLog(request, logModuleConsts.ZHGL, "退出登录"
            //        , e.getMessage(), null, "用户退出登录失败");
            json.put("code", 1);
            json.put("errMsg", e.getMessage());
        }
        Tools.sendResponseText(response, json.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.ZHGL, "退出登录"
                , null, json.toString());

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(401);
    }
}
