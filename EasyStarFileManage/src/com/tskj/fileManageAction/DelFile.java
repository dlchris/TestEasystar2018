package com.tskj.fileManageAction;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.fileManageService.FileManageService;
import com.tskj.fileManageService.Impl.FileManageServiceImpl;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

//取消挂接，删除原件
@WebServlet(name = "DelFile", urlPatterns = "/DelFile.do")
public class DelFile extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("删除原件。。。");
        FileManageService fileManage = new FileManageServiceImpl();
        JSONObject sendJson = new JSONObject();

        JSONObject jsonRet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String fileID = jsonRet.getString("fileID");
        fileID = fileID.substring(1, fileID.length() - 1).replace("\"", "");
        String docID = jsonRet.getString("docID");
        String[] fileIDs = fileID.split(",");
        if (fileIDs == null || fileIDs.length <= 0) {
            sendJson.put("code", 1);
            sendJson.put("msg", "原件ID不能为空！");
            Tools.sendResponseText(response, sendJson.toJSONString());
            LogUtil.emptySErrorLog(request, logModuleConsts.YJGL, "删除原件"
                    , sendJson.toString(), null, sendJson.toString());
            return;
        }
        List<Map<String, Object>> fileList = fileManage.getFileInfoByID(fileIDs);       //查询文件名

        //删除原件条目
        int ret = fileManage.batchDelFile(fileIDs);
//        int ret = 0;
        if (ret == 0) {
            //删除原件实体
            ResourceBundle systemRes = ResourceBundle.getBundle("fileEntity");
            String delFileAddress = systemRes.getString("delFileEntity");
            if (fileList == null || fileList.isEmpty()) {
                sendJson.put("code", 0);
                sendJson.put("msg", "原件删除成功！");
                Tools.sendResponseText(response, sendJson.toJSONString());
                LogUtil.emptySessionLog(request, logModuleConsts.YJGL, "删除原件", null, sendJson.toString());
                return;
            }
            JSONObject delJson = new JSONObject();
            delJson.put("fileList", JSON.toJSONString(fileList));
            delJson.put("docID", docID);
            String jsonRes = Tools.sendPostRequest(delFileAddress, delJson.toJSONString());
            System.out.println(jsonRes);

            sendJson.put("code", 0);
            sendJson.put("msg", "原件删除成功！");
        } else {
            sendJson.put("code", 1);
            sendJson.put("msg", "原件删除失败！");
        }
        Tools.sendResponseText(response, sendJson.toJSONString());
        LogUtil.emptySessionLog(request, logModuleConsts.YJGL, "删除原件", null, sendJson.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

}
