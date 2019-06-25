package com.easystar.action.classTree;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.dao.ClassTree;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.user.dao.UserInfo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * 得到门类列表
 *
 * @author LeonSu
 */
@WebServlet(name = "GetClassTreeAction", urlPatterns = "/ClassTree.do")
public class GetClassTreeAction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonObject = JSONObject.parseObject("{}");
//        String originHeader = request.getHeader("Origin");
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpSession session = request.getSession();
        //UserInfo userInfo = (UserInfo) session.getAttribute("userInfo");
        UserInfo userInfo = sessionDataImpl.getUserInfo();
        ClassTree classTree = new ClassTree();
        List<Map<String, Object>> classListAll = classTree.getClassTree(userInfo.getUserId(), userInfo.getRoleId());
        JSONArray classList = JSONArray.parseArray("[]");
        JSONObject classObject;
        for (Map<String, Object> obj : classListAll) {
            classObject = JSONObject.parseObject("{}");
            classObject.put("CLASSID", obj.get("newClassId"));
            classObject.put("DESCRIPTION", obj.get("DESCRIPTION"));
            classObject.put("CLASSTYPE", obj.get("CLASSTYPE"));
            classList.add(classObject);
        }

        session.setAttribute("allClassInfo", classListAll);

        jsonObject.put("code", 0);
        jsonObject.put("list", classList);
        Tools.sendResponseText(response, jsonObject.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "获取档案库列表", null, jsonObject.toString());
    }
}

