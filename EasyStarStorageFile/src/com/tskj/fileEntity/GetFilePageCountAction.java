package com.tskj.fileEntity;

import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.pdf.Pdf;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet(name = "GetFilePageCountAction", urlPatterns = "/GetFilePageCountAction.do")
public class GetFilePageCountAction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("获取原件总页数...");
        JSONObject jsonSend = JSONObject.parseObject("{}");
        JSONObject jsonRet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String docID = jsonRet.getString("docID");
        String fileID = jsonRet.getString("fileID");

        ResourceBundle systemRes = ResourceBundle.getBundle("filePath");
        //获取存储路径
        String mergeSavePath = systemRes.getString("mergeFilePath");
        String splitSavePath = systemRes.getString("splitFilePath");
        File file = new File(splitSavePath);
        if(!file.exists()){
            file.mkdirs();
        }

        String filePath = mergeSavePath + "\\"+ docID;
        String splitFile = splitSavePath + "\\" + fileID;         //按名称拆分
        //根据标题分割利用包，得到要传输的PDF文件
        if(!new File(splitFile).exists()){
            Pdf pdf = new Pdf(filePath, false);
            pdf.split(splitFile, fileID);
        }

        Pdf splitPdf = new Pdf(splitFile, false);
        int pageCount = splitPdf.getPageCount();
        jsonSend.put("code", 0);
        jsonSend.put("count", pageCount);
        Tools.sendResponseText(response, jsonSend.toJSONString());

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
