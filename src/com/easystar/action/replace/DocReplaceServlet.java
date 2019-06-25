package com.easystar.action.replace;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.utility.Tools;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.replace.biz.EasyStarReplace;
import com.tskj.replace.impl.DocReplace;
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
@WebServlet(name = "DocReplaceServlet", urlPatterns = "/DocReplace.do")
public class DocReplaceServlet extends HttpServlet {
    private String tableName;//对应表名
    private String perFixDes;//档号组成项
    private String classType;//档案类别

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }

        JSONObject json = JSONObject.parseObject(Tools.getStringFromRequest(request));

        /*HttpSession session = request.getSession();
        ClassTreeInfo classTreeInfo = (ClassTreeInfo) session.getAttribute("classInfo");*/
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
       /* } catch (Exception e) {
            e.printStackTrace();
            json.put("code", 1);
            json.put("errMsg", e.getMessage());
        }*/
            ClassTreeInfo classTreeInfo = sessionDataImpl.getClassTreeInfo();
            tableName = classTreeInfo.getDocTable();
            perFixDes = classTreeInfo.getPerFixDes();
            //classType  看EasyStarConsts类 0.文件级 1.盒级 2.案卷级
            // classtype 一定要用session 中获取的classtype 不能是classTreeInfo中的
            classType = sessionDataImpl.getClassType();
            System.err.println("classType值:" + classType);
            DocNoEngine docNoEngine = new DocNoEngine(tableName, classType, perFixDes);
            //try {
            EasyStarReplace replace = new DocReplace(classTreeInfo.getRealClassId(), classTreeInfo.getDocTable(), json, docNoEngine);
            json = replace.replace();
        } catch (Exception e) {
            e.printStackTrace();
            json.put("code", 1);
            json.put("errMsg", e.getMessage());
        }
        LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "文件级成批替换", null, json.toString());
        Tools.sendResponseText(response, json.toString());
    }

}
