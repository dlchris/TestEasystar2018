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
import java.sql.SQLException;
import java.util.*;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-05-23 11:21
 **/
@WebServlet(name = "UserFunctionServlet", urlPatterns = "/UserFunction.do")
public class UserFunctionServlet extends HttpServlet {
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

    //保存用户功能权限
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonSend = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONArray datas = jsonGet.getJSONArray("datas");
        List<Map<String, Object>> list = JSONArray.parseObject(datas.toString(), List.class);
        //List list1 = JSON.parseObject(datas.toString(), List.class);
        //JSONArray.parseArray(datas)
        System.err.println(list);
        //if (list != null) {
        String userId = jsonGet.getString("userId");
        System.err.println(datas);
        try {
            String s = ums.resetUserRight(userId, list);
            System.err.println(s);
            Tools.sendResponseText(response, s);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "数据库保存失败");
            Tools.sendResponseText(response, jsonSend.toString());
        }
        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "保存用户功能权限", null, jsonSend.toString());

    }

    //获取用户功能权限列表和用户已选中的数据
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonSend = new JSONObject();
        SessionDataBiz sessionDataImpl = getpermanent(request);
        String menuType = request.getParameter("menuType").trim();
        String userId = request.getParameter("userId").trim();
        if (menuType != null && userId != null && !userId.isEmpty() && !menuType.isEmpty()) {
            //所有对应菜单
            List<Map<String, Object>> userRightByMenuType = ums.getUserRightByMenuType(userId, menuType);
            List<Map<String, Object>> menuInfo = rms.getMenuInfo(menuType);
            jsonSend.put("code", 0);
            jsonSend.put("userMenu", menuTree(menuInfo));//角色功能权限列表
            jsonSend.put("userRight", menuTree(userRightByMenuType));//角色选中的功能权限列表
            //System.err.println("角色功能权限列表:" + menuTree(menuInfo));
            //System.err.println("选中的权限列表:" + menuTree(roleRightByMenuType));
        } else {

            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "传参有误,检测传值是否正确");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "获取用户功能权限列表和用户已选中的数据"
                , null, jsonSend.toString());

    }

    private List<Map<String, Object>> menuTree(List<Map<String, Object>> menuInfo) {
        TreeSet<String> set = new TreeSet<>();
        List<Map<String, Object>> array2 = new ArrayList<>();
        for (Map<String, Object> map : menuInfo) {
            boolean mainmenu = set.add(map.get("MAINMENU").toString());//添加成功的是第一次
        }
        //System.err.println(set);
        for (String s : set) {
            List<Map<String, Object>> array = new ArrayList<>();
            for (Map<String, Object> map : menuInfo) {
                if (map.get("MAINMENU").equals(s)) {
                    Map<String, Object> menuTree = new HashMap<>();
                    menuTree.put("label", map.get("SUBMENU"));
                    menuTree.put("id", map.get("MENUID"));
                    array.add(menuTree);
                }
            }
            Map<String, Object> menuTreeParent = new HashMap<>();
            menuTreeParent.put("id", "");
            menuTreeParent.put("label", s);
            menuTreeParent.put("children", array);
            array2.add(menuTreeParent);
        }
        return array2;
    }
}
