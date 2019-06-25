package com.easystar.action;

import com.tskj.core.config.ConfigUtility;
import com.tskj.core.system.consts.DBConsts;
import com.tskj.core.system.ip.IPUtil;
import com.tskj.core.system.ip.IPWhiteListUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet(name = "GetDBConfigServlet", urlPatterns = "/GetDBConfigAction")
public class GetDBConfigServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sourceIp = IPUtil.getIpAddr(request);
        IPWhiteListUtil ipWhiteListUtil = new IPWhiteListUtil();
        if (!ipWhiteListUtil.checkIpList(sourceIp)) {
            response.setStatus(404);
            return;
        }

        ConfigUtility configUtility = new ConfigUtility();
        FileInputStream fis = (FileInputStream) configUtility.readDbConfig(DBConsts.DB_CONFIG_FILENAME);
        if (fis != null) {
            response.setContentType("text/plain");
            // 得到输出流
            OutputStream output = response.getOutputStream();
            // 输入缓冲流
            BufferedInputStream bis = new BufferedInputStream(fis);
            // 输出缓冲流
            BufferedOutputStream bos = new BufferedOutputStream(output);
            // 缓冲字节数
            byte[] data = new byte[4096];
            int size = bis.read(data);
            while (size != -1) {
                bos.write(data, 0, size);
                size = bis.read(data);
            }
            bis.close();
            bos.flush();// 清空输出缓冲流
            bos.close();
        }
    }
}
