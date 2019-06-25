package com.tskj.fileimport.servlet;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.docframe.dao.DocFrameManager;
import com.tskj.fileimport.system.CFileImport;
import com.tskj.fileimport.system.CFilesImport;
import com.tskj.fileimport.system.Tools;
import com.tskj.fileimport.system.consts.DataTypeConsts;
import com.tskj.fileimport.system.db.DbUtility;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 验证字段的有效性，来源数据中的重复性
 *
 * @author LeonSu
 */
@WebServlet(name = "VerifyFieldAction", urlPatterns = "/VerifyFieldAction.do")
public class VerifyFieldAction extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String originHeader = request.getHeader("Origin");
        originHeader = originHeader == null ? "" : originHeader;

        JSONObject jsonReturn = JSONObject.parseObject("{}");
        JSONObject jsonReceive = JSONObject.parseObject(Tools.getStringFromRequest(request));

        if (!jsonReceive.containsKey("type")) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errmsg", "参数不足，未知类型");
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            return;
        }
        HttpSession session = request.getSession();
        if (session == null) {
            jsonReturn.put("result", 2);
            jsonReturn.put("errMsg", "请先登录");
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            return;
        }
        Object xlsFileName = request.getSession().getAttribute("filename");
        ClassTreeInfo classInfo = (ClassTreeInfo) request.getSession().getAttribute("classInfo");
        String classId = classInfo.getRealClassId();
        //System.err.println("类别ID" + classId);
        Object classType = request.getSession().getAttribute("classType").toString();
        //System.err.println("类型:" + classType);
        if (xlsFileName == null || classId == null || classType == null) {
            jsonReturn.put("result", 2);
            jsonReturn.put("errmsg", "请重新上传");
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            return;
        }

        CFilesImport fileImport = new CFilesImport(xlsFileName.toString(), "..\\..\\TempImportFile\\");
        String sourceFieldName, targetFieldName;
        switch (jsonReceive.getString("type")) {
            case "1":
                //来源数据中的重复性
                sourceFieldName = jsonReceive.getString("fieldname");
                boolean verfiy = false;
                try {
                    verfiy = fileImport.verfiy(sourceFieldName);
                } catch (Exception e) {
                    e.printStackTrace();
                    jsonReturn.put("result", 1);
                    jsonReturn.put("errmsg", "校验失败原因:" + e.getMessage());
                }
                System.err.println(verfiy);
                if (!verfiy) {
                    jsonReturn.put("result", 2);
                    jsonReturn.put("errmsg", "来源数据主键内容不唯一或主键存在空值");
                } else {
                    jsonReturn.put("result", 0);
                    jsonReturn.put("errmsg", "");
                }
                break;
            case "2":
                //来源字段与目标字段的数据类型与长度
                sourceFieldName = jsonReceive.getString("sourcefieldname");//excal字段
                targetFieldName = jsonReceive.getString("targetfieldname");//目标字段
                DocFrameManager docFrameManager = new DocFrameManager();
                try {
                    String tableName = "";
                    String sql = "SELECT * FROM classtree where classid='" + classId.toString() + "'";
                    List<Map<String, Object>> list = DbUtility.execSQL(sql);
                    switch (classType.toString()) {
                        case "0":
                            tableName = list.get(0).get("DOCTABLE").toString().trim();
                            break;
                        case "1":
                            tableName = list.get(0).get("BOXTABLE").toString().trim();
                            break;
                        case "2":
                            tableName = list.get(0).get("ROOLTABLE").toString().trim();
                            break;
                        default:
                    }
                    list = docFrameManager.getDocframe(tableName, "FIELDTYPE,FIELDSIZE", targetFieldName);
                    if (null == list || list.size() == 0) {
                        jsonReturn.put("result", 1);
                        jsonReturn.put("errmsg", "校验失败");
                        break;
                    }
                    DataTypeConsts dataTypeConsts = DataTypeConsts.DT_STRING;
                    int fieldSize = 0;
                    boolean b = false;
                    switch (list.get(0).get("FIELDTYPE").toString()) {
                        case "B":
                            dataTypeConsts = DataTypeConsts.DT_STRING;
                            fieldSize = Integer.valueOf(list.get(0).get("FIELDSIZE").toString());
                            break;
                        case "C":
                        case "V":
                            dataTypeConsts = DataTypeConsts.DT_STRING;
                            fieldSize = Integer.valueOf(list.get(0).get("FIELDSIZE").toString());
                            break;
                        case "T":
                        case "D":
                            dataTypeConsts = DataTypeConsts.DT_STRING;
                            fieldSize = 8;
                            break;
                        case "I":
                            dataTypeConsts = DataTypeConsts.DT_INTEGER;
                            fieldSize = 0;
                            break;
                        default:
                            jsonReturn.put("result", 1);
                            jsonReturn.put("errmsg", "未知的数据类型");
                            b = true;
                            break;
                    }
                    if (!b) {
                        JSONObject verifyResult = fileImport.verifyDataType(sourceFieldName, dataTypeConsts, fieldSize, targetFieldName);
                        if (verifyResult != null) {
                            jsonReturn.put("result", verifyResult.get("result"));
                            jsonReturn.put("errmsg", verifyResult.get("errmsg"));
                        } else {
                            jsonReturn.put("result", 1);
                            jsonReturn.put("errmsg", "校验失败");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    jsonReturn.put("result", 1);
                    jsonReturn.put("errmsg", "校验失败");
                }
                break;
            default:
        }
        Tools.sendResponseText(response, originHeader, jsonReturn.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(404);
    }
}
