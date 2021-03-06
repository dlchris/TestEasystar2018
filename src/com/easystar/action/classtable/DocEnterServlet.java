package com.easystar.action.classtable;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.impl.EnterClassTable;
import com.tskj.core.system.consts.EasyStarConsts;
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

/**
 * @author LeonSu
 */
@WebServlet(name = "DocEnterServlet", urlPatterns = "/SelectDoc.do")
public class DocEnterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject ret = JSONObject.parseObject("{}");
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            ret.put("code", 1);
            ret.put("errMsg", "档案库不正确");
        } else {
            HttpSession session = request.getSession();
            EnterClassTable.enter(session, EasyStarConsts.DOC);
            ret.put("code", 0);
        }

        Tools.sendResponseText(response, ret.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "进入文件级管理", null, ret.toString());
    }
}
