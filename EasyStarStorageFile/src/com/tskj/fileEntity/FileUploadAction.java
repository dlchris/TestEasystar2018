package com.tskj.fileEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.config.ConfigUtility;
import com.tskj.core.system.utility.Tools;
import com.tskj.util.StrUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
* @Description: 文件上传,  接收值docID，以及上传的文件
* @param
* @return
* @author Mao
* @date 2019/3/6 10:27
*/
@WebServlet(name = "FileUploadAction", urlPatterns = "/FileUploadAction.do")
public class FileUploadAction extends javax.servlet.http.HttpServlet {
    public Cookie getCookie(Cookie[] cookies, String cookieName) {
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(cookieName)) {
                return cookie;
            }
        }
        return null;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        System.out.println("文件上传...");
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        //获取配置文件system.properties信息
        ResourceBundle systemRes = ResourceBundle.getBundle("filePath");

        JSONObject ret = new JSONObject();
        String originHeader = request.getHeader("Origin");
        String classId = "";
        HttpSession session = request.getSession();
        Cookie cookie = getCookie(request.getCookies(), "JSESSIONID");
        if (cookie == null) {
            cookie = new Cookie("JSESSIONID", session.getId());
        }
        System.out.println(cookie.getValue());
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        cookie.setHttpOnly(true);

        /**
         * 从应用服务器中获取档案门类ID
         * @author LeonSu
         */

        String getClassIdUrl = systemRes.getString("getClassId");
        String get = Tools.sendPostRequest(getClassIdUrl, "", cookie.getValue());
        ret = JSONObject.parseObject("get");
        if (ret.getInteger("code") != 0 || !ret.containsKey("classId")) {
            ret.clear();
            ret.put("code", 1);
            ret.put("msg", "文件上传失败！");
            Tools.sendResponseText(response, ret.toString());
            return;
        }
        classId = ret.getString("classId");
        ret.clear();;
        System.out.println("classid = " + classId);
        /**
         * end
         */


        if(session.getAttribute("classInfo") != null){
            ClassTreeInfo classInfo = (ClassTreeInfo) session.getAttribute("classInfo");           //TODO 如果是分开部署拿到classID
            classId = classInfo.getRealClassId();
        }
        response.addCookie(cookie);

        //获取存储路径
        String savePath = systemRes.getString("savePath");
        File file = new File(savePath);
        if(!file.exists()){
            file.mkdir();
        }
        String msg = "";
        try {
            // 使用Apache文件上传组件处理文件上传步骤：
            // 1.创建一个DiskFileItemFactory工厂
            DiskFileItemFactory factroy = new DiskFileItemFactory();
            // 2.创建一个文件上传解析器
            ServletFileUpload upload = new ServletFileUpload(factroy);
            // 解决文件上传中的中文乱码问题
            upload.setHeaderEncoding("UTF-8");
            // 3.判断提交上来的数据是否是上传带文件表单的数据
            if (!ServletFileUpload.isMultipartContent(request)) {
                ret.put("code", 1);
                ret.put("msg", "表单必须包含 enctype=multipart/form-data");
                Tools.sendResponseText(response, originHeader, ret.toJSONString());
                return;
            }
//            upload.setFileSizeMax(1024*1024*10);         //单个文件
//            upload.setSizeMax(1024*1024*100);            //所有文件大小

            // 4.使用ServletFileUpload解析器解析上传数据，
            // 解析结果返回的是一个List<FileItem>，每一个FileItem对应一个Form表单的输入项
            List<FileItem> list = upload.parseRequest(request);
            String docID = "";       //条目ID
            String uploadType = "";       //扫描还是原件上传   0.原件上传 1.扫描
            String creatTime = "";        //文件创建时间
            String docNo = "";        //档号
            List<Map<String, Object>> fileList = new ArrayList<>();

            for(FileItem item : list){
                if(item.isFormField()){   //判断是表单域还是文件域
                    String name = item.getFieldName();    //获取表单中file的name属性值
                    if(name != null && "docID".equals(name)){
                        // 解决普通输入项的数据的中文乱码问题
                        docID = item.getString("UTF-8");         //获取条目ID

                    }/*else if (name != null && "uploadType".equals(name)){//todo 增加区分类别 是扫描还是原件上传
                        uploadType = item.getString("UTF-8");         //扫描还是原件上传
                    }else if (name != null && "docNo".equals(name)){//获取前台docNo
                        docNo = item.getString("UTF-8");         //扫描还是原件上传
                    }*/

                } else {    //如果fileItem中封装的是上传文件
                    // 如果fileItem中封装的是上传文件

                    long fileSize = item.getSize()/1024;//上传文件的大小 单位KB
                    String filename = item.getName();     //获取文件名

                    // 得到上传的文件名称 trim():去掉字符串首尾的空格。
                    if (filename == null || filename.trim().equals("")) {
                        continue;
                    }
                    filename = filename.substring(filename.lastIndexOf("\\") + 1);   //真实文件名
                    String fileType = filename.substring(filename.lastIndexOf(".")+1);   //拓展名
                    if(!"pdf".equals(fileType.toLowerCase())){        //如果上传文件不是PDF，跳过
                        continue;
                    }
                    String fileID = StrUtil.getUUID32();     //文件ID
                    String saveName = fileID + "." + fileType;   //存储文件名

                    Map<String, Object> fileInfo = new HashMap<>();      //保存文件信息
                    fileInfo.put("FILEID", fileID);
                    fileInfo.put("CLASSID", classId);
                    fileInfo.put("FILENAME", saveName);
                    fileInfo.put("FILESIZE", fileSize);
                    fileInfo.put("FILETYPE", fileType);
                    fileInfo.put("DESCRIOTIO", filename);
                    fileInfo.put("PATH", classId);
                    fileInfo.put("SYSFILENAM", fileID);
                    fileList.add(fileInfo);

                    // 获取item中的上传文件的输入流
                    InputStream in = item.getInputStream();
                    FileOutputStream out = new FileOutputStream(savePath + "\\"+ fileID);

                    // 创建一个缓冲区
                    byte[] buffer = new byte[1024];
                    // 判断输入流中的数据是否已经读完的标识
                    int len = 0;
                    // 循环将输入流读入到缓冲区中，(len=in.read(buffer))>0就表示in里面还有数据,把数据放到buffer中，放满一次项文件写入一次直到全部写入到文件中
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    // 关闭输入流
                    in.close();
                    // 关闭输出流
                    out.close();
                    // 删除处理文件上传时生成的临时文件
                    item.delete();

                }
            }

            //判断上传信息是否包含文件
            if(fileList == null || fileList.isEmpty()){
                ret.put("code", 1);
                ret.put("msg", "上传内容不包含文件");
                Tools.sendResponseText(response, originHeader, ret.toJSONString());
                return;
            }

            //文件合成利用包
            MergeFile mergeFile = new MergeFile();
            mergeFile.mergeFile(docID, fileList);

            //文件挂接
//            String jsonRes = "{\"msg\":\"挂接成功！\",\"code\":0}";
            String hangUpAddress = systemRes.getString("hangUpFile");
            JSONObject sendJson = new JSONObject();
            sendJson.put("docID", docID);
            sendJson.put("datas", fileList);
            //挂接结果
            String jsonRes = Tools.sendPostRequest(hangUpAddress, sendJson.toJSONString());
            System.out.println(jsonRes);
            Tools.sendResponseText(response, originHeader, jsonRes);
            return;

        } catch (FileUploadBase.FileSizeLimitExceededException e){
            e.printStackTrace();
            ret.put("code", 1);
            ret.put("msg", "单个文件超出最大值！");
        } catch (FileUploadBase.SizeLimitExceededException e) {
            e.printStackTrace();
            ret.put("code", 1);
            ret.put("msg", "上传文件的总的大小超出限制的最大值！");
        } catch (FileUploadException e) {
            e.printStackTrace();
            ret.put("code", 1);
            ret.put("msg", "文件上传失败！");
        }
        Tools.sendResponseText(response, originHeader, ret.toJSONString());

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        response.setStatus(401);
    }
}
