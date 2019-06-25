package com.easystar.action.replace;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.utility.Tools;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.replace.biz.EasyStarReplace;
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
@WebServlet(name = "RoolReplaceServlet", urlPatterns = "/RoolReplace.do")
public class RoolReplaceServlet extends HttpServlet {
    private String tableName;//对应表名
    private String perFixDes;//档号组成项
    private String classType;//档案类别

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }
        JSONObject json = JSONObject.parseObject(Tools.getStringFromRequest(request));
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
            HttpSession session = request.getSession();
            ClassTreeInfo classTreeInfo = (ClassTreeInfo) session.getAttribute("classInfo");
            perFixDes = classTreeInfo.getPerFixDes();
            tableName = classTreeInfo.getRoolTable();
            classType = session.getAttribute("classType").toString();
            DocNoEngine docNoEngine = new DocNoEngine(tableName, classType, perFixDes);
            //try {
            EasyStarReplace replace = new RoolReplace(classTreeInfo.getRealClassId(), classTreeInfo.getRoolTable(), json, docNoEngine);
            json = replace.replace();
           /* } catch (Exception e) {
                e.printStackTrace();
                json.put("code", 1);
                json.put("errMsg", e.getMessage());
            }*/

        } catch (Exception e) {
            e.printStackTrace();
            json.put("code", 1);
            json.put("errMsg", e.getMessage());
        }
        LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "案卷级成批替换", null, json.toString());
        Tools.sendResponseText(response, json.toString());

    }

}
