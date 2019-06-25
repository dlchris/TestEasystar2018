package com.easystar.action.files;

import com.alibaba.fastjson.JSONObject;
import com.easystar.system.files.FileManager;
import com.tskj.core.system.utility.Tools;
import com.tskj.fileManageService.FileManageService;
import com.tskj.fileManageService.Impl.FileManageServiceImpl;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GetFilesServlet", urlPatterns = "/GetFiles.do")
public class GetFilesServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("搜索原件。。。");
        FileManageService fileManage = new FileManageServiceImpl();
        JSONObject sendJson = new JSONObject();
        JSONObject jsonRet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String fileName = jsonRet.getString("fileName");
        String docID = jsonRet.getString("docID");

        List<Map<String, Object>> fileList = fileManage.searchFileByName(fileName, docID);
        sendJson.put("code", 0);
        sendJson.put("list", fileList);
        Tools.sendResponseText(response, sendJson.toJSONString());
        LogUtil.info(sessionDataImpl, logModuleConsts.YJGL, "搜索条目下原件", null, sendJson.toString());

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String docId = request.getParameter("docId");
//        System.err.println(docId);
        FileManager fileManager = new FileManager();
        JSONObject jsonRet = JSONObject.parseObject("{}");
        List<Map<String, Object>> list = fileManager.getFileListByDocId(docId);
        try {
            if (list == null) {
                jsonRet.put("code", 1);
                jsonRet.put("errMsg", "error to get filename list");
                return;
            }
            jsonRet.put("code", 0);
            jsonRet.put("list", list);
        } finally {
//            System.out.println(jsonRet.toJSONString());
            Tools.sendResponseText(response, "", jsonRet.toJSONString());
            LogUtil.info(sessionDataImpl, logModuleConsts.YJGL, "获取条目下已挂接原件列表", null, jsonRet.toString());
        }
    }

}
