package com.tskj.user.action;

import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.user.userRightDAO.UserManageDAO;
import com.tskj.user.userRightDAOImpl.UserManageDAOImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-05-07 14:24
 **/
@WebServlet(name = "ChangeUserPwServlet", urlPatterns = "/ChangeUserPwServlet.do")
public class ChangeUserPwServlet extends HttpServlet {
    UserManageDAO umd = new UserManageDAOImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(404);
    }

    //修改密码
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String newPw = request.getParameter("newPw");
        JSONObject jsonSend = new JSONObject();
        try {
            SessionDataBiz session = PermanentDataSourceFactory.getSessionDataImpl(request);
            String userId = session.getUserInfo().getUserId();
            if (umd.changePassWord(userId, newPw) != 0) {
                jsonSend.put("code", 1);
                jsonSend.put("errMsg", "密码修改失败");
            } else {
                jsonSend.put("code", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "数据库修改失败");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "修改密码", null, jsonSend.toString());
    }
}
