package com.tskj.fileimport.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.docframe.dao.DocFrameManager;
import com.tskj.fileimport.system.CFileImport;
import com.tskj.fileimport.system.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Servlet implementation class FileUploadAction
 *
 * @author LeonSu
 */
@WebServlet(name = "TSFileUploadAction", urlPatterns = "/TSFileUploadAction.do")
public class FileUploadAction extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 上传文件存储目录
     */
    private static final String UPLOAD_DIRECTORY = "TempImportFile";

    /**
     * 上传配置
     */
    // 3MB
    private static final int MEMORY_THRESHOLD = 1024 * 1024 * 3;
    // 40MB
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 40;
    // 50MB
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("开始接收数据导入源文件");
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        String originHeader = request.getHeader("Origin");
        // 检测是否为多媒体上传
        if (!ServletFileUpload.isMultipartContent(request)) {
            // 如果不是则停止
            jsonReturn.put("result", 1);
            jsonReturn.put("error", "表单必须包含 enctype=multipart/form-data");
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            LogUtil.error(sessionDataImpl, logModuleConsts.DRGL,
                    "导入条目时上传excal", "表单必须包含 enctype=multipart/form-data", null, jsonReturn.toString());
            return;
        }

        /*
         * 用户连接上传时，删除之前上传的文件
         */
        Object tmpFileName = request.getSession().getAttribute("filename");
        if (tmpFileName != null && !tmpFileName.toString().isEmpty()) {
            CFileImport tmpFile = new CFileImport(tmpFileName.toString(), "..\\..\\TempImportFile\\");
            tmpFile.delFile();
        }

        JSONArray jsonFiles = JSONArray.parseArray("[]");
        JSONObject jsonFile;

        SimpleDateFormat sdfFileName = new SimpleDateFormat(
                "yyyyMMddHHmmssSSS");
        SimpleDateFormat uploadtimeFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        // 配置上传参数
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
        factory.setSizeThreshold(MEMORY_THRESHOLD);
        // 设置临时存储目录
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload upload = new ServletFileUpload(factory);

        // 设置最大文件上传值
        upload.setFileSizeMax(MAX_FILE_SIZE);

        // 设置最大请求值 (包含文件和表单数据)
        upload.setSizeMax(MAX_REQUEST_SIZE);

        // 构造临时路径来存储上传的文件
        // 这个路径相对当前应用的目录
        String uploadPath = getServletContext().getRealPath("./") + File.separator + UPLOAD_DIRECTORY;


        // 如果目录不存在则创建
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        try {
            // 解析请求的内容提取文件数据
            @SuppressWarnings("unchecked")
            List<FileItem> formItems = upload.parseRequest(request);
            String targetClassId = "";
            String targetClassType = "";
            String xlsFileName = "";
            if (formItems != null && formItems.size() > 0) {
                // 迭代表单数据
                for (FileItem item : formItems) {
                    // 处理不在表单中的字段
                    if (!item.isFormField()) {
                        //只接收第一个文件
                        if (!xlsFileName.isEmpty()) {
                            continue;
                        }
                        String fileName = new File(item.getName()).getName();
                        // 获取文件后缀名
                        String fileExt = fileName.substring(fileName
                                .lastIndexOf("."));
                        Date now = new Date();
                        // 文件名称
                        String newfileName = sdfFileName.format(now);
                        xlsFileName = newfileName + fileExt;
                        String uploadtime = uploadtimeFormat.format(now.getTime());
                        String filePath = uploadPath + File.separator + newfileName + fileExt;
                        File storeFile = new File(filePath);

                        // 保存文件到硬盘
                        item.write(storeFile);

                        CFileImport fileImport = new CFileImport(newfileName + fileExt, "..\\..\\TempImportFile\\");
                        if (fileImport == null) {
                            throw new Exception("文件格式不正确");
                        }

                        jsonFile = JSONObject.parseObject("{}");
                        jsonFile.put("filename", xlsFileName);
                        jsonFile.put("updatetime", uploadtime);
                        jsonFile.put("rows", String.valueOf(fileImport.getRowCount()));
                        // 获取excel中第一行作为列
                        JSONArray xlsFields = JSONArray.parseArray(JSONArray.toJSONString(fileImport.getColumns()));
                        jsonFile.put("fields", xlsFields);

                        jsonFiles.add(jsonFile);
                    } else {
                        // 处理是表单中的字段
                        switch (item.getFieldName().toUpperCase()) {
                            case "CLASSID":
                                System.out.println(item.getString());
                                targetClassId = item.getString();
                                break;
                            case "CLASSTYPE":
                                System.out.println(item.getString());
                                targetClassType = item.getString();
                            default:
                                break;
                        }
                        if (!targetClassId.isEmpty() && !targetClassType.isEmpty()) {
                            DocFrameManager docFrameManager = new DocFrameManager();
                            jsonReturn.put("tablefields", docFrameManager.getDocframe(targetClassId, targetClassType, "DISSTR,FIELDNAME"));
                        }
                    }
                }
                jsonReturn.put("files", jsonFiles);
            }
            jsonReturn.put("result", 0);
            request.getSession().setAttribute("uid", UUID.randomUUID().toString());
            request.getSession().setAttribute("filename", xlsFileName);
        } catch (Exception ex) {
            ex.printStackTrace();
            jsonReturn.put("result", 1);
            jsonReturn.put("error", ex.getMessage());
        }
        Tools.sendResponseText(response, originHeader, jsonReturn.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.DRGL, "导入条目时上传excal", null, jsonReturn.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

}
