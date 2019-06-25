package com.tskj.report;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.db.DbUtility;
import com.tskj.core.system.utility.Tools;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JsonDataSource;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LeonSu
 */
public class ReportManager {

    private ReportUtils reportUtils;
    public ReportManager() {
        reportUtils = new ReportUtils();
    }

    public String getContentType(DocType docType) {
        return reportUtils.getContentType(docType);
    }
    /**
     * 根据门类ID和门类类型，返回报表列表
     * @param classId
     * @param classType
     * @return
     */
    public List<Map<String, Object>> getReportList(String classId, String classType) {
        String sql = "SELECT REPID,REPNAME FROM repinfo WHERE classid='" + classId + "' AND reptype='" + classType + "' ORDER BY repname";
        System.err.println(sql);
        return DbUtility.execSQL(sql);
    }

    public Long print(HttpServletResponse response, DocType docType, String reportFileName, JSONArray jsonData) throws IOException {
        return print(response.getOutputStream(), docType, reportFileName, jsonData);
    }

    public boolean isSinglePage(String reportFileName) {
        File reportFile = new File(Tools.getClassPath("/report/").concat(reportFileName));
        return reportUtils.isSinglePage(reportFile);
    }

    public long print(OutputStream outputStream, DocType docType, String reportFileName, JSONArray jsonData) {
        try {
            File reportFile = new File(Tools.getClassPath("/report/").concat(reportFileName));
            JRDataSource jsonDataSource = new JsonDataSource(new ByteArrayInputStream(jsonData.toJSONString().getBytes("utf-8")));
            HashMap<String, Object> parameters = new HashMap<>(1);
            reportUtils.createExportDocument(outputStream, docType, reportFile.getPath(), parameters, jsonDataSource);
            return 0L;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return -1L;
        } catch (IOException e) {
            e.printStackTrace();
            return -1L;
        } catch (JRException e) {
            e.printStackTrace();
            return -1L;
        }
    }

    public Long print(PrintWriter writer, DocType docType, String reportFileName, JSONArray jsonData) {
        try {
            File reportFile = new File(Tools.getClassPath("/report/").concat(reportFileName));
            JRDataSource jsonDataSource = new JsonDataSource(new ByteArrayInputStream(jsonData.toJSONString().getBytes("utf-8")));
            HashMap<String, Object> parameters = new HashMap<>(1);
            return reportUtils.createExportDocument(writer, docType, reportFile.getPath(), parameters, jsonDataSource);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return -1L;
        } catch (IOException e) {
            e.printStackTrace();
            return -1L;
        } catch (JRException e) {
            e.printStackTrace();
            return -1L;
        }
    }
}
