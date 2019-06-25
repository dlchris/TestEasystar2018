package com.easystar.action.replace;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.utility.Tools;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.replace.biz.EasyStarReplace;
import com.tskj.replace.impl.BoxReplace;
import com.tskj.replace.impl.RoolReplace;
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
@WebServlet(name = "BoxReplaceServlet", urlPatterns = "/BoxReplace.do")
public class BoxReplaceServlet extends HttpServlet {
    private String tableName;//对应表名
    private String perFixDes;//档号组成项
    private String classType;//档案类别

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sdb = null;
        JSONObject json = JSONObject.parseObject(Tools.getStringFromRequest(request));
        try {
            sdb = PermanentDataSourceFactory.getSessionDataImpl(request);
            if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
                LogUtil.error(sdb, logModuleConsts.DAGL, "盒级成批替换", "调用失败", null, null);
                return;
            }


            HttpSession session = request.getSession();
            ClassTreeInfo classTreeInfo = (ClassTreeInfo) session.getAttribute("classInfo");
            tableName = classTreeInfo.getBoxTable();
            perFixDes = classTreeInfo.getPerFixDes();
            classType = session.getAttribute("classType").toString();
            DocNoEngine docNoEngine = new DocNoEngine(tableName, classType, perFixDes);
            EasyStarReplace replace = new BoxReplace(classTreeInfo.getRealClassId(), classTreeInfo.getBoxTable(), json, docNoEngine);
            json = replace.replace();
        } catch (Exception e) {
            e.printStackTrace();
            json.put("code", 1);
            json.put("errMsg", e.getMessage());
        }
        LogUtil.info(sdb, logModuleConsts.DAGL, "盒级成批替换", null, json.toString());
        Tools.sendResponseText(response, json.toString());
    }

}
