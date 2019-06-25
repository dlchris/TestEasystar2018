package com.tskj.role.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
import java.util.*;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-27 19:03
 **/
@WebServlet(name = "RoleFunServlet", urlPatterns = "/RoleFun.do")
public class RoleFunServlet extends HttpServlet {
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

    //获取角色对应的权限
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();

        //String roleId = request.getParameter("roleId").trim();
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String roleId = jsonGet.getString("roleId");
        if (roleId != null && !"".equals(roleId)) {//!"".equals(menuType) &&
            //所有菜单
            List<Map<String, Object>> roleRight = rms.getRoleRight(roleId);
            jsonSend.put("code", 0);
            jsonSend.put("roleRight", roleRight);//对应角色绑定的权限
            //jsonSend.put();
        } else {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "传参有误,检测传值是否正确");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "获取角色对应的权限", null, jsonSend.toString());
    }

    //获取角色功能菜单
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = getpermanent(request);
        JSONObject jsonSend = new JSONObject();
        String menuType = request.getParameter("menuType").trim();
        String roleId = request.getParameter("roleId").trim();
        if ("".equals(menuType) || "".equals(roleId)) {
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "传参有误,检测传值是否正确");
        } else {
            //所有对应菜单
            List<Map<String, Object>> menuInfo = rms.getMenuInfo(menuType);
            List<Map<String, Object>> roleRightByMenuType = rms.getRoleRightByMenuType(roleId, menuType);
            jsonSend.put("code", 0);
            //jsonSend.put("roleMenu", menuInfo);//角色功能权限列表
            jsonSend.put("roleMenu", menuTree(menuInfo));//角色功能权限列表
            jsonSend.put("roleRight", menuTree(roleRightByMenuType));//角色选中的功能权限列表
            //System.err.println("角色功能权限列表:" + menuTree(menuInfo));
            //System.err.println("选中的权限列表:" + menuTree(roleRightByMenuType));
            //jsonSend.put();
        }
        //System.err.println(JSONObject.toJSON(jsonSend));
        //Object o = JSONObject.toJSON(jsonSend);
        //System.err.println( JSONObject.toJSON(jsonSend));
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.JSGL, "获取角色功能菜单", null, jsonSend.toString());
    }

    private List<Map<String, Object>> menuTree(List<Map<String, Object>> menuInfo) {
        List<Map<String, Object>> array2 = new ArrayList<>();
        //menuTree
        TreeSet<String> set = new TreeSet<>();
        for (Map<String, Object> map : menuInfo) {
            boolean mainmenu = set.add(map.get("MAINMENU").toString());//添加成功的是第一次
        }
        //System.err.println(set);
        for (String s : set) {
            List<Map<String, Object>> array = new ArrayList<>();
            for (Map<String, Object> map : menuInfo) {
                if (map.get("MAINMENU").equals(s)) {
                    Map<String, Object> menuTree = new HashMap<>();
                    menuTree.put("id", map.get("MENUID"));
                    menuTree.put("label", map.get("SUBMENU"));
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

    public static void main(String[] args) {
        RoleFunServlet rfs = new RoleFunServlet();
        rfs.menuTree(null);

    }

    /* const data = [{
        id: 1,
        label: '一级 1',
        children: [{
          id: 4,
          label: '二级 1-1',
          children: [{
            id: 9,
            label: '三级 1-1-1'
          }, {
            id: 10,
            label: '三级 1-1-2'
          }]
        }]
      }, {
        id: 2,
        label: '一级 2',
        children: [{
          id: 5,
          label: '二级 2-1'
        }, {
          id: 6,
          label: '二级 2-2'
        }]
      }, {
        id: 3,
        label: '一级 3',
        children: [{
          id: 7,
          label: '二级 3-1'
        }, {
          id: 8,
          label: '二级 3-2'
        }]
      }];*/


}
