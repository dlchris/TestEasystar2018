package com.easystar.action.docframe;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.utility.Tools;
import com.tskj.docframe.dao.DocFrameManager;
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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetDocFrameServlet", urlPatterns = "/GetDocFrame.do")
public class GetDocFrameServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonObject = JSONObject.parseObject("{}");
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //LogUtil.info(sessionDataImpl, logModuleConsts.ZDGL, "获取档案库所用的字典", null, jsonObject.toString());
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }

        //HttpSession session = request.getSession();
        //ClassTreeInfo classTreeInfo = (ClassTreeInfo) session.getAttribute("classInfo");

        ClassTreeInfo classTreeInfo = sessionDataImpl.getClassTreeInfo();
        String classId = classTreeInfo.getRealClassId();
        String classType = request.getParameter("classType");
        DocFrameManager docFrameManager = new DocFrameManager();
        List<Map<String, Object>> cols;
        try {

            cols = docFrameManager.getDocframe(classId, classType, "DISSTR, FIELDNAME, FIELDSIZE, FIELDTYPE, DICTTABLE, DOCNO, FIELDSTATE");
            jsonObject.put("code", 0);
            jsonObject.put("cols", cols);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", "Get field failure");
        }
        Tools.sendResponseText(response, "", jsonObject.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "获取档案库结构", null, jsonObject.toString());
    }
}
