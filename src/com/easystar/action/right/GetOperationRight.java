package com.easystar.action.right;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetOperationRight", urlPatterns = "/GetOperationRight.do")
public class GetOperationRight extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }


    /*
     * 导出：非标准档案导出
     * 导入：非标准档案导入
     * 打印：权限都有
     * */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("获取操作权限。。。");
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = JSONObject.parseObject("{}");
        HttpSession session = request.getSession();
        //UserInfo userInfo = (UserInfo) session.getAttribute("userInfo");
        UserInfo userInfo = sessionDataImpl.getUserInfo();
        String mainMenu = request.getParameter("mainMenu");
        //获取主菜单  menuType=2
        UserManageService userManage = new UserManageServiceImpl();
        List<Map<String, Object>> list = userManage.getOperationRight(userInfo.getUserId(), userInfo.getRoleId(), "2", mainMenu);            //增删改查
        List<Map<String, Object>> list2 = userManage.getOperationRight(userInfo.getUserId(), userInfo.getRoleId(), "2", "其它功能与设置");    //导出
        list.addAll(list2);
        JSONArray menuList = JSON.parseArray(JSON.toJSONString(list));

        session.setAttribute("subMenu", list);

        jsonObject.put("code", 0);
        jsonObject.put("list", menuList);
        Tools.sendResponseText(response, jsonObject.toString());

        LogUtil.info(sessionDataImpl, logModuleConsts.QXGL, "获取模块下操作权限", null, jsonObject.toString());
    }
}
