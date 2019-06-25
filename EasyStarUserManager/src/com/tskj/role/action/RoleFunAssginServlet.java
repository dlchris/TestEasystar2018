package com.tskj.role.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.role.biz.RoleManagerService;
import com.tskj.role.biz.impl.RoleManagerServiceImpl;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import org.apache.poi.ss.formula.functions.T;

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
 * @create: 2019-04-27 17:15
 **/
@WebServlet(name = "RoleFunAssginServlet", urlPatterns = "/RoleFunAssgin.do")
public class RoleFunAssginServlet extends HttpServlet {
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

    //新增,修改角色权限
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        JSONArray datas = jsonGet.getJSONArray("datas");
        List<Map<String, Object>> list = JSONArray.parseObject(datas.toString(), List.class);
        //List list1 = JSON.parseObject(datas.toString(), List.class);
        //JSONArray.parseArray(datas)
        System.err.println(list);
        //if (list != null) {
        String roleId = jsonGet.getString("roleId");
        System.err.println(datas);
        try {
            String s = rms.resetRoleRight(roleId, list);
            System.err.println(s);
            Tools.sendResponseText(response, s);
            LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "新增,修改角色权限", null, s);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "数据库保存失败");
            Tools.sendResponseText(response, jsonSend.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "新增,修改角色权限", null, jsonSend.toString());
        }

        //} else {
        //jsonSend.put("code", 1);
        //jsonSend.put("errMsg", "");
        //}
    }

    //获取角色功能,菜单权限列表
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonSend = new JSONObject();
        //String menuType = request.getParameter("menuType").trim();
        String roleId = request.getParameter("roleId").trim();
        SessionDataBiz sessionDataImpl = getpermanent(request);
        if (!"".equals(roleId)) {//!"".equals(menuType) &&
            //所有菜单
            List<Map<String, Object>> menuInfo = rms.getMenuInfo("-1");
            List<Map<String, Object>> roleRight = rms.getRoleRight(roleId);
            jsonSend.put("code", 0);
            jsonSend.put("roleMenu", menuInfo);//角色功能权限列表
            jsonSend.put("roleRight", roleRight);//对应角色绑定的权限
            //jsonSend.put();
        } else {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "传参有误,检测传值是否正确");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "获取角色功能,菜单权限列表", null, jsonSend.toString());


    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        super.doDelete(req, resp);
    }
}
