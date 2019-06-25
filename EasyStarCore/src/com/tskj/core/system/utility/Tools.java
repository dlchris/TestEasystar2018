package com.tskj.core.system.utility;

import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HTTP;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * 工具类，主要提供一些常用或通用的方法，比如servlet的响应结果，使用post方式调用别的servlet，从request中获取数据体内容等等
 * 不定期更新
 * getRequestJson、sendRespone、sendPostRequest
 * @author LeonSu
 * @date 2018-09-26
 * @version 1.0
 *
 */
public class Tools {
    public static List<Map<String, Object>> reportIdList = new ArrayList<>();

    public static int serverPort = 8080;

    public static List<HttpServletResponse> uploadMsgList = new ArrayList<>(10);

    // 跨域域名设置
    public static final String[] ALLOW_DOMAIN = {
            "http://localhost:80"
            ,"http://127.0.0.1:8080"
            ,"http://192.168.0.100"
            ,"http://127.0.0.1:8020"
            ,"http://localhost:8080"
    };

    public static String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    /**
     * 返回classes目录的绝对路径
     * @return
     *  成功返回非空字符串，否则返回空白字符串
     */
    public static String getClassPath() {
        return getClassPath("/");
    }

    /**
     * 返回classes目录下的指定目录名的绝对路径
     * @return
     *  成功返回非空字符串，否则返回空白字符串
     */
    public static String getClassPath(@NotNull String subPath) {
        try {
            return URLDecoder.decode(new Object() {
                public String getPath(String path) {
                    String tmpPath = path;
                    if (tmpPath != null && !tmpPath.isEmpty()) {
                        if (tmpPath.charAt(0) != '/') {
                            tmpPath = "/" + tmpPath;
                        }
                    } else {
                        tmpPath = "/";
                    }
                    try {
                        tmpPath = this.getClass().getResource(tmpPath).getPath();
                        return tmpPath;
                    } catch (NullPointerException e) {
                        return "";
                    }
                }
            }.getPath(subPath), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
    /**
     * 从HttpServletRequest中的数据流中读取json
     *
     * @param request
     * @return
     *  JSONObject
     */
    @Deprecated
    public static JSONObject getJsonFromRequest(HttpServletRequest request) {
        return JSONObject.parseObject(getStringFromRequest(request));
    }

    /**
     * 从HttpServletRequest中的数据流中读取json
     *
     * @param request
     * @return
     *  String
     */
    public static String getStringFromRequest(HttpServletRequest request) {
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));
            StringBuffer sb = new StringBuffer("");
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sendResponseText(ServletResponse response1, String originHeader, String msg, String charset) {
        try {
            HttpServletResponse response = (HttpServletResponse)response1;
            response.setHeader("Access-Control-Allow-Credentials", "true");
//            System.out.println("----------------------------------------");
//            System.out.println(originHeader);
            if (originHeader != null && !originHeader.isEmpty()) {
                if (Arrays.asList(ALLOW_DOMAIN).contains(originHeader)) {
//                    response.setHeader("Access-Control-Allow-Origin", originHeader);
                }
            } else {
//                response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
            }
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST,GET,PUT,DELETE");
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

    public static void sendResponseText(ServletResponse response, String msg) {
        sendResponseText(response, null, msg, "");
    }

    /**
     * 采用post方式，utf-8编码调用别的servlet，并返回utf-8编码的处理结果
     * @param url，servlet的地址，不需要包含主机
     * @param jsonText
     * @return
     */
    public static String sendPostRequest(String url, String jsonText) {
        return sendPostRequest(url, jsonText, "");
    }

    public static String sendPostRequest(String url, String jsonText, String sessionId) {
        Logs.log("发给：" + jsonText);
        CookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", sessionId);
        cookie.setPath("/");
        cookie.setDomain("liba.com");
        cookieStore.addCookie(cookie);
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        try {

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
            httpPost.setHeader(HTTP.CONTENT_ENCODING, "UTF-8");
            StringEntity se = new StringEntity(jsonText, "utf-8");
            se.setContentEncoding("UTF-8");
            httpPost.setEntity(se);
            HttpResponse httpResponse = client.execute(httpPost);
            if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                HttpEntity entity = httpResponse.getEntity();
                StringBuilder result = new StringBuilder();
                InputStream inputStream = entity.getContent();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String s;
                while (((s = reader.readLine()) != null)) {
                    result.append(s);
                }
                reader.close();
                Logs.log("发送处理结果得到的信息：" + result.toString());
                return result.toString();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    public static boolean checkClassInfoSession(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jsonObject = JSONObject.parseObject("{}");
        HttpSession session = request.getSession();
        if (session.getAttribute("classType") == null || session.getAttribute("classInfo") == null) {
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", "没有档案库信息");
            Tools.sendResponseText(response, jsonObject.toString());
            return false;
        }
        return true;
    }


    /**
     * 字符串工具
     *
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        return (obj == null ? "" : obj.toString());
    }

}
