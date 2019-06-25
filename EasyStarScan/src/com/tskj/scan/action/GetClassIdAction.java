package com.tskj.scan.action;

import com.alibaba.fastjson.JSONObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author LeonSu
 */
@WebServlet(name = "GetClientInterface", urlPatterns = "/GetClassIdAction.do")
public class GetClassIdAction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject json = JSONObject.parseObject("{}");
//        String jsonGetString = Tools.getStringFromRequest(request);
//        if (jsonGetString.isEmpty()) {
//            json.put("code", 1);
//            json.put("errMsg", "缺少参数");
//            Tools.sendResponseText(response, json.toString());
//            return;
//        }
        try {
            String classId = PermanentDataSourceFactory.getSessionDataImpl(request).getClassId();
            System.out.println(json.toString());
            json.put("code", 0);
            json.put("classId", classId);
            Tools.sendResponseText(response, json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            json.put("code", 1);
            json.put("errMsg", e.getMessage());
            Tools.sendResponseText(response, json.toString());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
