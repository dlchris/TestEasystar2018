package com.easystar.action.report;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.biz.ClassTableImpl;
import com.tskj.classtable.impl.ClassTableBoxImpl;
import com.tskj.classtable.impl.ClassTableDocImpl;
import com.tskj.classtable.impl.ClassTableRoolImpl;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.report.DocType;
import com.tskj.report.ReportManager;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.user.dao.UserInfo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LeonSu
 */
@WebServlet(name = "PrintReportServlet", urlPatterns = "/ReportPrint.do")
public class PrintReportServlet extends HttpServlet {

    private ReportManager reportManager;
    public static List<Map<String, Object>> reportIdList = new ArrayList<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonObject = JSONObject.parseObject("{}");
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }

        if (!Tools.checkClassInfoSession(request, response)) {
            return;
        }

        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));

        // 测试时使用 --- START
//        String jsonStr = "{\n" +
//                "    \"classId\": \"7114C8BDC95041119DFE7226F525CC94\",\n" +
//                "    \"tableName\": \"DOCUMENT88050C556E4E4988\",\n" +
//                "    \"reportName\": \"归档文件目录（横）\",\n" +
//                "    \"list\": [\n" +
//                "        \"F690AAF5ED3547939B8C33478939DB39\",\n" +
//                "        \"CEE192ABFED74EA6B102D6E65DD1E0DF\",\n" +
//                "        \"4A47DB9D42734447B4326F06BD9FC90B\"\n" +
//                "    ]\n" +
//                "}";
//        JSONObject jsonGet = JSONObject.parseObject(jsonStr);
        // 测试时使用 --- END

        HttpSession session = request.getSession();
        String reportId = Tools.newId();
        Map<String, Object> map = new HashMap<>(1);
        //jsonGet.put("classInfo", (ClassTreeInfo) session.getAttribute("classInfo"));
        //jsonGet.put("classType", session.getAttribute("classType").toString());
        jsonGet.put("classInfo", sessionDataImpl.getClassTreeInfo());
        jsonGet.put("classType", sessionDataImpl.getClassType());

        map.put(reportId, jsonGet);
        PrintReportServlet.reportIdList.add(map);
//        session.setAttribute(reportId, jsonGet);
        jsonObject.put("code", 0);
        jsonObject.put("id", reportId);
        Tools.sendResponseText(response, jsonObject.toString());

        LogUtil.info(sessionDataImpl, logModuleConsts.BBGL, "打印报表数据", null, jsonObject.toString());
//            JRDataSource jsonDataSource = new JsonDataSource(new ByteArrayInputStream(jsonData.toJSONString().getBytes("utf-8")));
//            InputStream is = new ByteArrayInputStream(jsonData.toJSONString().getBytes("utf-8"));
//            HashMap<String, Object> parameters = new HashMap<>(10);
//            parameters.put("JSON_INPUT_STREAM", is);
////            JasperViewer.viewReport(reportFile.getPath(), false);
//            byte[] bytes = JasperRunManager.runReportToPdf(reportFile.getPath(), parameters, jsonDataSource);
//            if (bytes == null) {
//                response.setContentType("application/json;charset=utf-8");
//                PrintWriter wr = response.getWriter();
//                wr.write("{\"code\": 1,\"errMsg\":\"无数据\"}");
//                wr.flush();
//                wr.close();
//            } else {
//                response.setContentType("application/pdf");
//                response.addHeader("Content-Disposition", "attachment; filename=report.pdf");
//                response.setContentLength(bytes.length);
//                ServletOutputStream outputStream = response.getOutputStream();
//                outputStream.write(bytes, 0, bytes.length);
//                outputStream.flush();
//                outputStream.close();
//            }
//        } catch (JRException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonObject = JSONObject.parseObject("{}");
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }

//        if (!Tools.checkClassInfoSession(request, response)) {
//            return;
//        }
//
//        HttpSession session = request.getSession();

        String reportId = request.getParameter("id");
        String fileName = request.getParameter("name");

        JSONObject jsonGet = null;
        for (Map<String, Object> map : PrintReportServlet.reportIdList) {
            if (map.keySet().toArray()[0].toString().equals(reportId)) {
                jsonGet = (JSONObject) map.get(reportId);
                PrintReportServlet.reportIdList.remove(map);
                break;
            }
        }
        if (jsonGet == null) {
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", "没有档案库信息");
            Tools.sendResponseText(response, jsonObject.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.BBGL, "预览报表", null, jsonObject.toString());
            return;
        }
        // 测试时使用 --- START
//        String jsonStr = "{\n" +
//                "    \"classId\": \"7114C8BDC95041119DFE7226F525CC94\",\n" +
//                "    \"tableName\": \"DOCUMENT88050C556E4E4988\",\n" +
//                "    \"reportName\": \"归档文件目录（横）\",\n" +
//                "    \"list\": [\n" +
//                "        \"F690AAF5ED3547939B8C33478939DB39\",\n" +
//                "        \"CEE192ABFED74EA6B102D6E65DD1E0DF\",\n" +
//                "        \"4A47DB9D42734447B4326F06BD9FC90B\"\n" +
//                "    ]\n" +
//                "}";
//        JSONObject jsonGet = JSONObject.parseObject(jsonStr);
//        String classType = "0";
//        ClassTableImpl classTable = null;
//        try {
//            classTable = new ClassTableDocImpl(jsonGet.getString("classId"), jsonGet.getString("tableName"), "");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        // 测试时使用 --- END

        String reportFileName = jsonGet.getString("reportName").concat(".jasper");
        File file = new File(Tools.getClassPath("/report/") + reportFileName);
        if (!file.exists()) {
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", "报表不存在");
            Tools.sendResponseText(response, jsonObject.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.BBGL, "预览报表", null, jsonObject.toString());
            return;
        }

        ClassTreeInfo classTreeInfo = (ClassTreeInfo) jsonGet.get("classInfo");
        String classType = jsonGet.getString("classType");
        String sortFieldName;
        ClassTableImpl classTable;
        try {
            switch (classType) {
                case "0":
                    classTable = new ClassTableDocImpl(classTreeInfo.getRealClassId(), classTreeInfo.getDocTable(), classTreeInfo.getPerFixDes());
                    sortFieldName = "NOTENO";
                    break;
                case "1":
                    classTable = new ClassTableBoxImpl(classTreeInfo.getRealClassId(), classTreeInfo.getBoxTable(), classTreeInfo.getPerFixDes());
                    sortFieldName = "CASENO";
                    break;
                case "2":
                    classTable = new ClassTableRoolImpl(classTreeInfo.getRealClassId(), classTreeInfo.getRoolTable(), classTreeInfo.getPerFixDes());
                    sortFieldName = "CASENO";
                    break;
                default:
                    jsonObject.put("code", 1);
                    jsonObject.put("errMsg", "没有档案库信息");
                    Tools.sendResponseText(response, jsonObject.toString());
                    LogUtil.info(sessionDataImpl, logModuleConsts.BBGL, "预览报表", null, jsonObject.toString());
                    return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", e.getMessage());
            Tools.sendResponseText(response, jsonObject.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.BBGL, "预览报表", null, jsonObject.toString());
            return;
        }

        Object[] values = jsonGet.getJSONArray("list").toArray();
        List<Map<String, Object>> tmpList = classTable.getList(values, sortFieldName);
//        try {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setHeader("Content-Disposition",
                "inline; filename*=UTF-8''" + URLEncoder.encode(fileName, "UTF-8"));

        ReportManager reportManager = new ReportManager();
        response.setContentType(reportManager.getContentType(DocType.PDF));

        JSONArray list = JSONArray.parseArray("[]");

        if (tmpList.size() > 0) {
            list = JSONArray.parseArray(JSONObject.toJSONString(tmpList));

            if (reportManager.isSinglePage(reportFileName)) {

                //单页，备考表
                //只取第一条，计算起件号与止件号
                JSONObject listItem = list.getJSONObject(0);

                String minNoteNo = listItem.getString("件号");
                String maxNoteNo = listItem.getString("件号");
                for (Object item : list.toArray()) {
                    if (minNoteNo.compareToIgnoreCase(((JSONObject) item).getString("件号")) > 0) {
                        maxNoteNo = ((JSONObject) item).getString("件号");
                    }
                    if (maxNoteNo.compareToIgnoreCase(((JSONObject) item).getString("件号")) < 0) {
                        maxNoteNo = ((JSONObject) item).getString("件号");
                    }
                }
                listItem.put("起件号", minNoteNo);
                listItem.put("止件号", maxNoteNo);
                list.clear();
                list.add(listItem);
            }
        }


        if (reportManager.print(response.getOutputStream(), DocType.PDF, reportFileName, list) != 0) {
            response.setHeader("Content-Type", "text/plain");
            PrintWriter wr = response.getWriter();
            wr.write("创建报表数据出错");
            wr.flush();
            wr.close();
        }
        LogUtil.info(sessionDataImpl, logModuleConsts.BBGL, "预览报表", null, jsonObject.toString());
    }
}
