package com.tskj.scan.action;

import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author LeonSu
 */
@WebServlet(name = "GetClientInterface", urlPatterns = "/GetClientInterface.do")
public class GetClientInterface extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject json = JSONObject.parseObject("{}");
        json.put("upload", "/FileUploadAction.do");
//        json.put("")
        Tools.sendResponseText(response, json.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(404);
    }
}
