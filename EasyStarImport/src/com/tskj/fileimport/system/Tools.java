package com.tskj.fileimport.system;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;

public class Tools extends com.tskj.core.system.utility.Tools {
    public static int serverPort = 8080;

    public static List<HttpServletResponse> uploadMsgList = new ArrayList<>(10);

    // 跨域域名设置
    public static final String[] ALLOW_DOMAIN = { "http://localhost:80", "http://127.0.0.1:8080",
            "http://192.168.0.100", "http://127.0.0.1:8020" };

//    /**
//     * 从HttpServletRequest中的数据流中读取json
//     *
//     * @param request
//     * @return
//     */
//    @Deprecated
//    public static JSONObject getJsonFromRequest(HttpServletRequest request) {
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));
//            StringBuffer sb = new StringBuffer("");
//            String temp;
//            while ((temp = br.readLine()) != null) {
//                sb.append(temp);
//            }
//            br.close();
//            return JSONObject.parseObject(sb.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 修改配置文件中的属性值
     * @param bundleName
     *      配置文件名，比如：system.properties
     * @param keyName
     *      属性的名称
     * @param keyValue
     *      属性的值
     * @return
     *      true：修改成功；false：修改失败
     */
    public static boolean setConfigValue(String bundleName, String keyName, String keyValue) {
        // 获取src下的文件路径
        String propertiesFileName = new Object() {
            public String getPath() {
                return this.getClass().getResource("/").getPath();
            }
        }.getPath().concat(bundleName);
        boolean writeOK = true;
        SafeProperties p = new SafeProperties();
        FileInputStream in;
        try {
            propertiesFileName = URLDecoder.decode(propertiesFileName, "utf-8");
            in = new FileInputStream(propertiesFileName);
            p.load(in);//
            in.close();
            p.setProperty(keyName, keyValue);// 设置属性值，如不属性不存在新建
            FileOutputStream out = new FileOutputStream(propertiesFileName);// 输出流
            p.store(out);// 设置属性头，如不想设置，请把后面一个用""替换掉
            out.flush();// 清空缓存，写入磁盘
            out.close();// 关闭输出流
        } catch (Exception e) {
            e.printStackTrace();
            writeOK = false;
        }
        return writeOK;
    }

    /**
     * 修改配置文件（system.properties）中的属性值
     * @param keyName
     *      属性的名称
     * @param keyValue
     *      属性的值
     * @return
     *      true：修改成功；false：修改失败
     */
    public static boolean setConfigValue(String keyName, String keyValue) {
        return setConfigValue("system.properties", keyName, keyValue);
    }

    /**
     * 获取配置文件中的值
     *
     * @param bundleName 配置文件的名称，不需要扩展名，默认的扩展名是：properties
     * @param keyName    key
     * @return
     */
    public static String getConfigValue(String bundleName, String keyName) {
        String propertiesFileName = new Object() {
            public String getPath() {
                return this.getClass().getResource("/").getPath();
            }
        }.getPath().concat(bundleName);
        SafeProperties p = new SafeProperties();
        FileInputStream in;
        try {
            propertiesFileName = URLDecoder.decode(propertiesFileName, "utf-8");
            in = new FileInputStream(propertiesFileName);
            p.load(in);//
            in.close();
            return p.getProperty(keyName, "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
//
//        ResourceBundle res = ResourceBundle.getBundle(bundleName);
//        if (res.containsKey(keyName)) {
//            try {
//                return new String(res.getString(keyName.trim()).getBytes("ISO-8859-1"), "GBK");
//            } catch (UnsupportedEncodingException e) {
//                return "";
//            }
//        }
//        return "";
    }

    /**
     * 获取配置文件（system.properties）中的值
     *
     * @param keyName key
     * @return
     */
    public static String getConfigValue(String keyName) {
        return getConfigValue("system.properties", keyName);
    }

    public static void sendResponseText(ServletResponse response1, String originHeader, String msg, String charset) {
        try {
            HttpServletResponse response = (HttpServletResponse)response1;
            response.setHeader("Access-Control-Allow-Credentials", "true");
            if (originHeader != null) {
                if (originHeader != null && Arrays.asList(ALLOW_DOMAIN).contains(originHeader)) {
                    System.out.println("----------------------------------------");
                    System.out.println(originHeader);
                    System.out.println("----------------------------------------");
                    response.setHeader("Access-Control-Allow-Origin", originHeader);
                }
            }
            response.setHeader("Access-Control-Allow-Methods", "POST");
            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");
            if (null != charset && !charset.isEmpty()) {
                response.setContentLength(URLEncoder.encode(msg, charset).length());
            }
            PrintWriter wr = response.getWriter();
            if (null != charset && !charset.isEmpty()) {
                wr.write(URLEncoder.encode(msg, "utf-8"));
            } else {
                wr.write(msg);
            }
            wr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendResponseText(ServletResponse response, String originHeader, String msg) {
        sendResponseText(response, originHeader, msg, "");
    }
    // 判断文件夹是否存在
    public static void judeDirExists(File file) {
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static String createTargetPath(String rootPath, String url) {
        URL url1 = null;
        try {
            url1 = new URL(url);
            String[] paths = url1.getPath().split("/");
            String tmpPath = rootPath;
            File tmpFile = null;
            for (int i = 2; i < paths.length - 1; i++) {
                tmpPath += "\\" + paths[i];
                tmpFile = new File(tmpPath);
                if (!tmpFile.exists()) {
                    tmpFile.mkdir();
                }
            }
            return tmpPath;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static final Pattern PATTERN = Pattern.compile("^[-\\+]?[\\d]*$");

    /*方法二：推荐，速度最快
     * 判断是否为整数
     * @param str 传入的字符串
     * @return 是整数返回true,否则返回false
     */
    public static boolean isInteger(String str) {
        return PATTERN.matcher(str).matches();
    }

}
