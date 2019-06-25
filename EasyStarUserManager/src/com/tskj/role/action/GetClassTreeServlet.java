package com.tskj.role.action;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.dao.ClassTree;
import com.tskj.core.system.utility.Tools;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.tools.Tool;
import java.io.IOException;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-04-27 19:27
 **/
@WebServlet(name = "GetClassTreeServlet", urlPatterns = "/GetClassTree.do")
public class GetClassTreeServlet extends HttpServlet {
    private ClassTree ct = new ClassTree();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    //获取档案库
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //获取所有档案库名称
        JSONObject classTree = ct.getClassTree(0);
        Tools.sendResponseText(response, classTree.toString());
    }
}
