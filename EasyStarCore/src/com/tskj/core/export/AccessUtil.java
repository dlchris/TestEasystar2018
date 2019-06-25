package com.tskj.core.export;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.healthmarketscience.jackcess.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class AccessUtil {


    public static void getAccessFile(String downloadPath, JSONArray jsonArray) {
        //1、复制文件
//        copyFile(sourePath, downloadPath);
        File file = new File(downloadPath);

        //2、设定字段+字段数据类型
        Set<String> keys = ((JSONObject) jsonArray.get(0)).keySet();
        List<String> titles = new ArrayList<>(keys.size());
        for (String key : keys) {
            titles.add(key);
        }

        try {
            Database base = DatabaseBuilder.create(Database.FileFormat.V2000, file);
            TableBuilder newTable = new TableBuilder("entryList");
            for (int i = 0; i < titles.size(); i++) {
                //设置字段（表头）
                newTable.addColumn(new ColumnBuilder(titles.get(i)).setSQLType(Types.VARCHAR));
            }
            Table table = newTable.toTable(base);
            //3、插入数据
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                List<String> list = new ArrayList<>();
                for (int j = 0; j < titles.size(); j++) {
                    list.add(jsonObj.getString(titles.get(j)));
                }
                table.addRow(list.toArray());
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description: 复制文件
     * @param sourceFile 源文件
     * @param buildFile 结果文件
     * @return
     * @author Mao
     * @date 2019/2/19 21:46
     */
    /*public static void copyFile(String sourceFile, String buildFile) {
        InputStream is = null;
        OutputStream out = null;
        try {
            is = new FileInputStream(sourceFile);
            out = new FileOutputStream(buildFile);
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = is.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
            is.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * @param
     * @return
     * @Description: 下载文件
     * @author Mao
     * @date 2019/2/20 11:42
     */
    public static void downLoadFile(String filePath, HttpServletResponse response) {    //
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("资源不存在！");
            return;
        }
        String fileName = file.getName();
//        System.out.println(fileName);
        try {
            //设置响应头，控制浏览器下载该文件
            response.setHeader("Content-disposition", "attachment; filename="
                    + new String((fileName).getBytes("utf-8"), "ISO8859-1"));
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            Date date = new Date();
            fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(date) + suffix;
            response.setHeader("FileName", fileName);
            response.setHeader("Access-Control-Expose-Headers", "FileName");
            //读取要下载的文件，保存到文件输入流
            FileInputStream in = new FileInputStream(filePath);
            //创建输出流
            OutputStream out = response.getOutputStream();
            //创建缓冲区
            byte buffer[] = new byte[1024];
            int len = 0;
            //循环将输入流中的内容读取到缓冲区当中
            while ((len = in.read(buffer)) > 0) {
                //输出缓冲区的内容到浏览器，实现文件下载
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
//            file.delete();   //删除生成的文件

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
