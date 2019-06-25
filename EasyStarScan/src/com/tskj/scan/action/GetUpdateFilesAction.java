package com.tskj.scan.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ResourceBundle;

/**
 * @author LeonSu
 */
@WebServlet(name = "getUpdateFilesAction", urlPatterns = "/getUpdateFiles.do")
public class GetUpdateFilesAction extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(200);
        String path = Tools.getClassPath("config/scan/");
        FileReader reader = new FileReader(path + "update.dat");
        BufferedReader inputStream = new BufferedReader(reader);
        String readoneline;
        JSONArray jsonRet = JSONArray.parseArray("[]");
        JSONObject jsonItem;
        while((readoneline = inputStream.readLine()) != null){
            if (!readoneline.isEmpty() && readoneline.trim().charAt(0) != '#') {
                String[] array = readoneline.split(" ");
                jsonItem = JSONObject.parseObject("{}");
                jsonItem.put("className", array[0]);
                jsonItem.put("version", array[1]);
                jsonRet.add(jsonItem);
            }
        }
        response.setContentType("application/json");
        response.setContentLength(jsonRet.size());
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(jsonRet.toJSONString().getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(404);
    }
}
