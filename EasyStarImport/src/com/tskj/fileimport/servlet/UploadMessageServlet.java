package com.tskj.fileimport.servlet;

import com.tskj.fileimport.SSE.listener.AppAsyncListener;
import com.tskj.fileimport.process.AsyncRequestProcessor;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * SSE服务器推送信息
 * @author LeonSu
 * @date 2018-10-22
 */
@WebServlet(name = "UploadMessageServlet", urlPatterns = "/UploadMessageServlet.do", asyncSupported = true)
public class UploadMessageServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        System.out.println(request.getHeader("Origin"));
        String uid = request.getSession().getAttribute("uid").toString();
        AsyncContext asyncCtx = request.startAsync();
        //asyncCtx.addListener(new AppAsyncListener());
        asyncCtx.setTimeout(300000);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) request.getServletContext().getAttribute("executor");
        AsyncRequestProcessor processor = new AsyncRequestProcessor(asyncCtx, uid);
        //request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        executor.execute(processor);
    }
}
