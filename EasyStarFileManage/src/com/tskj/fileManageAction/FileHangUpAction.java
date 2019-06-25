package com.tskj.fileManageAction;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.fileManageService.FileManageService;
import com.tskj.fileManageService.Impl.FileManageServiceImpl;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

//挂接
@WebServlet(name = "FileHangUpAction", urlPatterns = "/FileHangUpAction.do")
public class FileHangUpAction extends javax.servlet.http.HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("文件挂接。。。");
        FileManageService fileManage = new FileManageServiceImpl();
        JSONObject sendJson = new JSONObject();
        String jsonStr = Tools.getStringFromRequest(request);       //获取数据
        if (jsonStr == null) {
            sendJson.put("code", 2);
            sendJson.put("msg", "数据格式不正确");
            Tools.sendResponseText(response, sendJson.toJSONString());
            LogUtil.emptySErrorLog(request, logModuleConsts.YJGL, "挂接原件"
                    , sendJson.toString(), null, sendJson.toString());
            return;
        }
        JSONObject jsonObj = JSON.parseObject(jsonStr);
        String docID = jsonObj.getString("docID");
        List<Map<String, Object>> fileList = JSON.parseObject(jsonObj.getString("datas"), List.class);
        //文件挂接
        int ret = fileManage.hangupFile(docID, fileList);
        if (ret == 0) {
            sendJson.put("code", 0);
            sendJson.put("msg", "挂接成功！");
        } else {
            sendJson.put("code", 1);
            sendJson.put("msg", "挂接失败！");
        }
        Tools.sendResponseText(response, sendJson.toJSONString());
        LogUtil.emptySessionLog(request, logModuleConsts.YJGL, "挂接原件", null, sendJson.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }
}
