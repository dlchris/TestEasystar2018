package com.easystar.action.report;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.report.ReportManager;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author LeonSu
 */
@WebServlet(name = "ReportListServlet", urlPatterns = "/ReportList.do")
public class ReportListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }
        JSONObject json = JSONObject.parseObject("{}");

        if (!Tools.checkClassInfoSession(request, response)) {
            return;
        }

        HttpSession session = request.getSession();

        //ClassTreeInfo classTreeInfo = (ClassTreeInfo) session.getAttribute("classInfo");
        //String classType = session.getAttribute("classType").toString();
        ClassTreeInfo classTreeInfo = sessionDataImpl.getClassTreeInfo();
        String classType = sessionDataImpl.getClassType();

        ReportManager reportManager = new ReportManager();
        json.put("code", 0);
        json.put("list", reportManager.getReportList(classTreeInfo.getRealClassId(), classType));
        System.err.println(json.toString());
        Tools.sendResponseText(response, json.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "获取报表列表12", null, json.toString());
    }
}
