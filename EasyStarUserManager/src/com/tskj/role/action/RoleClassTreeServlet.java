package com.tskj.role.action;

import com.alibaba.fastjson.JSONArray;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-27 19:12
 **/
@WebServlet(name = "RoleClassTreeServlet", urlPatterns = "/roleClassTree.do")
public class RoleClassTreeServlet extends HttpServlet {
    private RoleManagerService rms = new RoleManagerServiceImpl();

    public SessionDataBiz getpermanent(HttpServletRequest request) {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sessionDataImpl;
    }

    //获取档案库对应的权限
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String classId = jsonGet.getString("CLASSID");
        String roleId = jsonGet.getString("ROLEID");
        if (!"".equals(classId) && !"".equals(roleId)) {
            Map<String, Object> roleModuleById = rms.findRoleModuleById(roleId, classId);
            jsonSend.put("code", 0);
            jsonSend.put("roleModule", roleModuleById);
        } else {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "参数不能为空");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "获取档案库对应的权限", null, jsonSend.toString());
    }

    //获取门类权限的分配
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonSend = new JSONObject();
        SessionDataBiz sessionDataImpl = getpermanent(request);
        //获取密级
        List<Map<String, Object>> security = new ArrayList<>();
        List<Map<String, Object>> department = new ArrayList<>();
        try {
            security = rms.getSysDict("SECURITY");
            department = rms.getSysDict("DEPARTMENT");
            //获取机构
            jsonSend.put("code", 0);
            jsonSend.put("SECURITY", security);
            jsonSend.put("DEPARTMENT", department);
        } catch (Exception e) {
            e.printStackTrace();
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "数据库查询出错");
        } finally {
            System.err.println(jsonSend);
            Tools.sendResponseText(response, jsonSend.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "获取门类权限的分配", null, jsonSend.toString());
        }
    }

    //修改权限
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(req);
        JSONObject jsonSend = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(req));
        String roleId = jsonGet.getString("roleId");//必填
        String moduleId = jsonGet.getString("moduleId");//必填
        //String security = jsonGet.getString("security");//可以为空
        //String security2 = jsonGet.getString("security2");//可以为空
        JSONArray security = null;
        JSONArray security2 = null;
        try {
            security = jsonGet.getJSONArray("security");
            security2 = jsonGet.getJSONArray("security2");
        } catch (Exception e) {
            e.printStackTrace();
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "格式有误!!!");
            Tools.sendResponseText(resp, jsonSend.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "修改权限", null, jsonSend.toString());
            return;
        }
        if (roleId != null && moduleId != null && !roleId.isEmpty() && !moduleId.isEmpty()) {
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
                int i = rms.saveRoleModule(roleId, moduleId, securityStr, securityStr2);
                if (i == 0) {
                    jsonSend.put("code", 0);
                } else {
                    jsonSend.put("code", 1);
                    jsonSend.put("errMsg", "保存失败");
                }
            }
            //Tools.sendResponseText(resp, jsonSend.toString());
        } else {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "数据参数不全");
            //Tools.sendResponseText(resp, jsonSend.toString());
        }
        Tools.sendResponseText(resp, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "修改权限", null, jsonSend.toString());
    }
}
