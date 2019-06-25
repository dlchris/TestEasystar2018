package com.easystar.action.right;

import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.user.dao.UserInfo;
import com.tskj.user.userRightService.UserManageService;
import com.tskj.user.userRightServiceImpl.UserManageServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-05-30 11:24
 **/
@WebServlet(name = "GetSysRightServlet", urlPatterns = "/GetSysRight.do")
public class GetSysRightServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JSONObject jsonSend = new JSONObject();
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
            UserInfo userInfo = sessionDataImpl.getUserInfo();
            UserManageService userManage = new UserManageServiceImpl();
            List<Map<String, Object>> list = userManage.getOperationRight(userInfo.getUserId(), userInfo.getRoleId(),
                    "4", "系统管理");  //增删改查
            jsonSend.put("code", 0);
            jsonSend.put("sysManageList", list);
        } catch (Exception e) {
            e.printStackTrace();
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "查询出错");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.QXGL, "获取系统管理操作权限", null, jsonSend.toString());

    }
}
