package com.easystar.action.classtable;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.classtree.dao.ClassTree;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 选择进入档案库
 *
 * @author LeonSu
 */
@WebServlet(name = "ClassTableServlet", urlPatterns = "/SelectClass.do")
public class ClassTableServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(Tools.getStringFromRequest(request));
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpSession session = request.getSession();

        List<Map<String, Object>> classListAll = (List<Map<String, Object>>) session.getAttribute("allClassInfo");
        boolean existsClassId = false;
        ClassTreeInfo classInfo;
        for (Map<String, Object> map : classListAll) {
            if (map.get("newClassId").toString().equalsIgnoreCase(jsonObject.getString("classId"))) {
                classInfo = new ClassTreeInfo();
                classInfo.setClassType(Integer.valueOf(map.get("CLASSTYPE").toString()));
                classInfo.setMapClassId(map.get("newClassId").toString().trim());
                classInfo.setRealClassId(map.get("CLASSID").toString().trim());
                classInfo.setDescription(map.get("DESCRIPTION").toString().trim());
                classInfo.setPerFixDes(map.get("PERFIXDES").toString().trim());
                if (map.get("BOXTABLE") != null) {
                    classInfo.setBoxTable(map.get("BOXTABLE").toString().trim());
                }
                if (map.get("DOCTABLE") != null) {
                    classInfo.setDocTable(map.get("DOCTABLE").toString().trim());
                }
                if (map.get("ROOLTABLE") != null) {
                    classInfo.setRoolTable(map.get("ROOLTABLE").toString().trim());
                }
                session.setAttribute("classInfo", classInfo);
                existsClassId = true;
                break;
            }
        }
        if (!existsClassId) {
            session.setAttribute("classInfo", null);
            jsonObject.clear();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", "档案库ID不正确");
        } else {
            jsonObject.clear();
            jsonObject.put("code", 0);
        }
        Tools.sendResponseText(response, jsonObject.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "进入档案库", null, jsonObject.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(404);
    }
}
