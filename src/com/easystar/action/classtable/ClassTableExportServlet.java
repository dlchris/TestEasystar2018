package com.easystar.action.classtable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.biz.ClassTableImpl;
import com.tskj.classtable.impl.ClassTableBoxImpl;
import com.tskj.classtable.impl.ClassTableDocImpl;
import com.tskj.classtable.impl.ClassTableRoolImpl;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.export.ExportUtil;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author LeonSu
 * @description 数据导出
 */
@WebServlet(name = "ExportDataServlet", urlPatterns = "/Export.do")
public class ClassTableExportServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonObject = JSONObject.parseObject("{}");

        // 测试时使用 --- START
//        String jsonStr = "{\n" +
//                "    \"classId\": \"7114C8BDC95041119DFE7226F525CC94\",\n" +
//                "    \"tableName\": \"DOCUMENT88050C556E4E4988\",\n" +
//                "    \"type\": \"0\",\n" +
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
        // 测试时使用 --- END

        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpSession session = request.getSession();
       /* if (session.getAttribute("classType") == null || session.getAttribute("classInfo") == null) {
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", "没有档案库信息");
            Tools.sendResponseText(response, jsonObject.toString());
            return;
        }*/

        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        //ClassTreeInfo classTreeInfo = (ClassTreeInfo) session.getAttribute("classInfo");
        ClassTreeInfo classTreeInfo = sessionDataImpl.getClassTreeInfo();
        ClassTableImpl classTable;
        try {
            switch (sessionDataImpl.getClassType().toString()) {
                case "0":
                    classTable = new ClassTableDocImpl(classTreeInfo.getRealClassId(), classTreeInfo.getDocTable(), classTreeInfo.getPerFixDes());
                    break;
                case "1":
                    classTable = new ClassTableBoxImpl(classTreeInfo.getRealClassId(), classTreeInfo.getBoxTable(), classTreeInfo.getPerFixDes());
                    break;
                case "2":
                    classTable = new ClassTableRoolImpl(classTreeInfo.getRealClassId(), classTreeInfo.getRoolTable(), classTreeInfo.getPerFixDes());
                    break;
                default:
                    jsonObject.put("code", 1);
                    jsonObject.put("errMsg", "档案库信息不正确");
                    return;
            }

            if (!jsonGet.containsKey("type") || jsonGet.getInteger("type") < 0 || jsonGet.getInteger("type") > 3) {
                jsonObject.put("code", 1);
                jsonObject.put("errMsg", "导出的文件格式不正确");
                Tools.sendResponseText(response, jsonObject.toString());
                return;
            }
            String fileType = jsonGet.getString("type");

            Object[] values = jsonGet.getJSONArray("list").toArray();
            List<Map<String, Object>> list = classTable.getList(values, "DOCNO");

            JSONArray jsonArray = JSONArray.parseArray(JSONObject.toJSONString(list));
            // 导出的文件名
            String fileName = "AAA";
            switch (fileType) {
                case "0":
                    // excel 2003
                    ExportUtil.exportToXls(fileName, jsonArray, response);
                    break;
                case "1":
                    // excel 2007
                    ExportUtil.exportToXlsx(fileName, jsonArray, response);
                    break;
                case "2":
                    // access 2003
                    ExportUtil.exportToMdb(fileName, jsonArray, response);
                    /*jsonObject.put("code", 1);
                    jsonObject.put("errMsg", "正在建设");
                    Tools.sendResponseText(response, jsonObject.toString());*/
                    break;
                case "3":
                    // access 2007
                    ExportUtil.exportToAccdb(fileName, jsonArray, response);
                    /*jsonObject.put("code", 1);
                    jsonObject.put("errMsg", "正在建设");
                    Tools.sendResponseText(response, jsonObject.toString());*/
                    break;
                default:
                    jsonObject.put("code", 1);
                    jsonObject.put("errMsg", "导出的文件格式不正确");
                    Tools.sendResponseText(response, jsonObject.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", e.getMessage());
            Tools.sendResponseText(response, jsonObject.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "数据导出", null, jsonObject.toString());
        }
    }

}
