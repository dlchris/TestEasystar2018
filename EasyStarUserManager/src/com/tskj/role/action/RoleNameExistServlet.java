package com.tskj.role.action;

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

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-26 09:10
 **/
@WebServlet(name = "RoleNameExistServlet", urlPatterns = "/RoleNameExist.do")
public class RoleNameExistServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonSend = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String roleName = Tools.toString(jsonGet.get("ROLENAME"));
        if ("".equals(roleName)) {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "角色名不能为空");
            Tools.sendResponseText(response, jsonSend.toJSONString());
            return;
        }
        RoleManagerService rms = new RoleManagerServiceImpl();
        jsonSend = rms.roleExists(roleName);
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "角色名是否存在", null, jsonSend.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(404);
    }
}
