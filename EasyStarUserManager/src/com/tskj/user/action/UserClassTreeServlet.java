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
import com.tskj.user.userRightDAO.UserManageDAO;
import com.tskj.user.userRightService.UserManageService;
import com.tskj.user.userRightServiceImpl.UserManageServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.FileNameMap;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-05-24 09:33
 **/
@WebServlet(name = "UserClassTreeServlet", urlPatterns = "/UserClassTree.do")
public class UserClassTreeServlet extends HttpServlet {
    public SessionDataBiz getpermanent(HttpServletRequest request) {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionDataImpl;
    }

    // 保存修改档案库对应的权限
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String userId = jsonGet.getString("userId");//必填
        String moduleId = jsonGet.getString("moduleId");//必填
        JSONArray security = null;
        JSONArray security2 = null;
        try {
            security = jsonGet.getJSONArray("security");
            security2 = jsonGet.getJSONArray("security2");
        } catch (Exception e) {
            e.printStackTrace();
            jsonSend.put("errMsg", "格式有误!!!");
            jsonSend.put("code", 1);
            Tools.sendResponseText(response, jsonSend.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "修改用户门类权限", null, jsonSend.toString());
            return;
        }
        if (userId != null && moduleId != null && !userId.isEmpty() && !moduleId.isEmpty()) {
            String securityStr = "";
            String securityStr2 = "";
            if (security != null && security.size() > 0) {
                for (Object o : security) {
                    securityStr += o + "+";
                }
            }
            if (security2 != null && security2.size() > 0) {
                for (Object o : security2) {
                    securityStr2 += o + "+";
                }
            }
            System.err.println(securityStr);
            System.err.println(securityStr2);
            if ("".equals(moduleId)) {
                jsonSend.put("code", 1);
                jsonSend.put("errMsg", "门类ID不能为空");
            } else {
                UserManageService ums = new UserManageServiceImpl();
                //ums
                int i = -1;
                try {
                    i = ums.saveUserModule(userId, moduleId, securityStr, securityStr2);
                    if (i == 0) {
                        jsonSend.put("code", 0);
                    } else {
                        jsonSend.put("code", 1);
                        jsonSend.put("errMsg", "保存失败");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    jsonSend.put("code", 1);
                    jsonSend.put("errMsg","修改出错" );
                }
            }
        } else {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "数据参数不全");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.YHGL, "修改用户门类权限", null, jsonSend.toString());
    }

    //获取用户对应档案库 对应密级、单位列表
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();
        String userId = request.getParameter("userId");
        String classId = request.getParameter("classId");
        if (userId != null && classId != null && !userId.isEmpty() && !classId.isEmpty()) {
            //查询档案库对应的密级和单位列表
            RoleManagerService rms = new RoleManagerServiceImpl();
            List<Map<String, Object>> department = rms.getDict(classId, "DEPARTMENT");
            List<Map<String, Object>> security = rms.getSysDict("SECURITY");
            //查询用户已选中的单位和密级列表
            UserManageService ums = new UserManageServiceImpl();
            List<Map<String, Object>> userModule = ums.userCTreePower(classId, userId);
            jsonSend.put("code", 0);
            //当前门类对应的密级列表
            jsonSend.put("SECURITY", security);
            //当前门类对应的单位列表
            jsonSend.put("DEPARTMENT", department);
            //当前门类权限已选中的密级和单位
            jsonSend.put("userModule", userModule);
        } else {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "数据传输不全");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.QXGL, "当前门类对应权限查询", null, jsonSend.toString());

    }
}
