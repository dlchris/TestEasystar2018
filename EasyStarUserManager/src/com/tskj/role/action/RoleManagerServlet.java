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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-25 12:39
 **/
@WebServlet(name = "RoleManagerServlet", urlPatterns = "/RoleManagerServlet.do")
public class RoleManagerServlet extends HttpServlet {
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

    /**
     * 创建角色
     *
     * @param request
     * @param response
     * @return
     * @Author JRX
     * @Description:
     * @create 2019/4/25 12:40
     **/
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        System.err.println(jsonGet);
        JSONObject data = jsonGet.getJSONObject("data");
        //判断保存还是修改后存储
        String jsonStr = rms.saveRole(data);
        Tools.sendResponseText(response, jsonStr);
        LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "创建角色", null, jsonStr);
    }

    /**
     * @param req
     * @param resp
     * @return
     * @Author JRX
     * @Description: 更新角色信息
     * @create 2019/4/25 13:30
     **/
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    /**
     * @param request
     * @param response
     * @return
     * @Author JRX
     * @Description: 通过roleId读取对应角色列表
     * @create 2019/4/25 12:40
     **/
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonObject = new JSONObject();
        SessionDataBiz sessionDataImpl = getpermanent(request);
        //默认值
        int pageIndex = 0;
        int pageSize = 20;
        try {
            pageIndex = Integer.valueOf(request.getParameter("pageIndex"));
            pageSize = Integer.valueOf(request.getParameter("pageSize"));
            List<Map<String, Object>> datas = rms.findAllRole(pageIndex, pageSize);
            int size = rms.countRole();
            jsonObject.put("code", 0);
            jsonObject.put("list", datas);
            jsonObject.put("count", size);
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", e.getMessage());
        }
        Tools.sendResponseText(response, jsonObject.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "通过roleId读取对应角色列表", null, null);
    }

    /**
     * @param req
     * @param resp
     * @return
     * @Author JRX
     * @Description: 刪除角色 TODO 删除底层表没有删全需要修改
     * @create 2019/4/25 13:42
     **/
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(req);
        JSONObject jsonStr = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(req));
        String roleId = Tools.toString(jsonGet.get("ROLEID"));
        if ("".equals(roleId)) {
            jsonStr.put("code", "1");
            jsonStr.put("errMsg", "角色ID不能为空");
            Tools.sendResponseText(resp, jsonStr.toJSONString());
            LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "刪除角色", null, jsonStr.toString());
            return;
        }
        try {
            String SendStr = rms.delRole(roleId);
            Tools.sendResponseText(resp, SendStr);
            LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "刪除角色", null, SendStr);
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            jsonStr.put("code", "1");
            jsonStr.put("errMsg", "SQL存储是出现错误!!!");
            Tools.sendResponseText(resp, jsonStr.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "刪除角色", null, jsonStr.toString());
            return;
        }
    }
}
