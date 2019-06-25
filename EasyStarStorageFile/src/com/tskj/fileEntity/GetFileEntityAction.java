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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ResourceBundle;

@WebServlet(name = "GetFileEntityAction", urlPatterns = "/GetFileEntityAction.do")
public class GetFileEntityAction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("分页获取原件实体。。。");
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setContentType("application/pdf");
        JSONObject sendJson = new JSONObject();
        JSONObject jsonRet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        String docID = jsonRet.getString("docID");
        String fileID = jsonRet.getString("fileID");
        String start = jsonRet.getString("beginPage");
        String end = jsonRet.getString("endPage");
        if(docID == null || "".equals(docID) || fileID == null || "".equals(fileID) || start == null || "".equals(start) || end == null || "".equals(end)){
            sendJson.put("code", 1);
            sendJson.put("msg", "参数不全！");
            Tools.sendResponseText(response, origin, sendJson.toJSONString());
            return;
        }
        int beginPage = Integer.parseInt(jsonRet.getString("beginPage"));
        int endPage = Integer.parseInt(jsonRet.getString("endPage"));


        ResourceBundle systemRes = ResourceBundle.getBundle("filePath");
        //获取存储路径
        String mergeSavePath = systemRes.getString("mergeFilePath");
        String splitSavePath = systemRes.getString("splitFilePath");
        File SplitFile = new File(splitSavePath);
        if(!SplitFile.exists()){
            SplitFile.mkdirs();
        }
        String filePath = mergeSavePath + "\\"+ docID;
        String splitFile = splitSavePath + "\\" + fileID;         //按名称拆分，没有删除

        //根据标题分割利用包，得到要传输的PDF文件
        if(!new File(splitFile).exists()){
            Pdf pdf = new Pdf(filePath, false);
            pdf.split(splitFile, fileID);
        }

        File file;
        if(endPage == -1){           //获取整个原件
            file = new File(splitFile);
        } else {
            String splitFileByPage = splitSavePath + "\\" + fileID + "_"+beginPage+"-"+endPage;      //按页码拆分
            //根据页数分割PDF文件
            Pdf splitPdf = new Pdf(splitFile, false);
            splitPdf.splitFileByPage(splitFileByPage, beginPage, endPage);
            file = new File(splitFileByPage);
        }

        //分割出来的利用包通过流传递到前台
        if(file.exists()){
            FileInputStream in = new FileInputStream(file);
            OutputStream out = response.getOutputStream();
            byte[] b = new byte[1024];
            int len = 0;
            while((len = in.read(b)) > 0) {
                out.write(b, 0, len);
            }
            out.flush();
            in.close();
            out.close();
            //删除按页码拆分出来的原件
            file.delete();
        } else {
            sendJson.put("code", 1);
            sendJson.put("msg", "获取原件失败");
            Tools.sendResponseText(response, origin, sendJson.toJSONString());
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    public static void main(String[] args) {
        String str = "-1";
        int num = Integer.parseInt(str);
        System.out.println(num);
    }
}
