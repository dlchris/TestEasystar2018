package com.tskj.fileEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

//删除原件实体
@WebServlet(name = "DelFileEntityAction", urlPatterns = "/DelFileEntityAction.do")
public class DelFileEntityAction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("删除原件实体。。。");
        JSONObject sendJson = new JSONObject();
        String jsonStr = Tools.getStringFromRequest(request);       //获取数据
        if (jsonStr == null) {
            sendJson.put("code", 1);
            sendJson.put("msg", "数据格式不正确");
            Tools.sendResponseText(response, sendJson.toJSONString());
            return;
        }
        ResourceBundle systemRes = ResourceBundle.getBundle("filePath");
        String savePath = systemRes.getString("savePath");    //文件地址
        String mergeFilePath = systemRes.getString("mergeFilePath");     //利用包地址
        String splitSavePath = systemRes.getString("splitFilePath");
        JSONObject jsonObj = JSON.parseObject(jsonStr);
        String files = jsonObj.getString("fileList");

        String docID = jsonObj.getString("docID");
        List<Map<String, Object>> fileList = JSON.parseObject(files, List.class);
        for (Map<String, Object> fileInfo : fileList){
            String fileID = fileInfo.get("FILEID").toString();
            //从利用包中移除
            MergeFile mergeFile = new MergeFile();
            mergeFile.delFileFromMerge(mergeFilePath+"\\"+docID, savePath+"\\"+fileID);
            //删除原件
            File file = new File(savePath + "\\" + fileID);
            if(file.exists()){
                file.delete();
            }
            //删除拆分文件
            File splitFile = new File(splitSavePath + "\\" + fileID);
            if (splitFile.exists()){
                splitFile.delete();
            }
        }
        sendJson.put("code", 0);
        sendJson.put("msg", "原件删除成功！");
        Tools.sendResponseText(response, sendJson.toJSONString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }


}
