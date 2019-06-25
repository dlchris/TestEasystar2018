package com.tskj.log.action;

import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.dao.SysLogsDao;
import com.tskj.log.dao.impl.SysLogsDaoImpl;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.session.bizImpl.SessionDataBizImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-05-30 10:15
 **/
@WebServlet(name = "SysLogsServlet", urlPatterns = "/SysLogs.do")
public class SysLogsServlet extends HttpServlet {
    SysLogsDao sld = new SysLogsDaoImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    //获取日志分页
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonSend = new JSONObject();
        int pageIndex = 0;
        int pageSize = 20;
        try {
            pageIndex = Integer.valueOf(request.getParameter("pageIndex"));
            pageSize = Integer.valueOf(request.getParameter("pageSize"));
            List<Map<String, Object>> list = sld.FindAllSysLogs(pageIndex, pageSize);
            int count = sld.countSysLogs();
            jsonSend.put("code", 0);
            jsonSend.put("list", list);
            jsonSend.put("count", count);
        } catch (Exception e) {
            e.printStackTrace();
            jsonSend.put("code", 1);
            jsonSend.put("errMsg", "传值有误");
        }
        Tools.sendResponseText(response, jsonSend.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.RZGL, "查看日志", null, null);
    }
}
