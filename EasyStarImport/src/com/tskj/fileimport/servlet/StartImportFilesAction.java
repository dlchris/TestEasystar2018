package com.tskj.fileimport.servlet;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.biz.ClassTableBiz;
import com.tskj.classtable.impl.ClassTableBoxImpl;
import com.tskj.classtable.impl.ClassTableDocImpl;
import com.tskj.classtable.impl.ClassTableRoolImpl;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.docno.bean.DocNoFieldInfo;
import com.tskj.docno.dao.DocNoDao;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.fileimport.process.DataManager;
import com.tskj.fileimport.process.DataProcess;
import com.tskj.fileimport.system.CFilesImport;
import com.tskj.fileimport.system.Tools;
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
import java.sql.SQLException;
import java.util.*;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-05 14:16
 **/
@WebServlet(name = "StartImportFilesAction", urlPatterns = "/StartImportFilesAction.do")
public class StartImportFilesAction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.err.println("测试是否改变");
        /*String requestedWith = request.getHeader("x-requested-with");
        if (requestedWith != null && requestedWith.equalsIgnoreCase("XMLHttpRequest")) {
            System.err.println("ajax");
        } else {
            System.err.println("正常方法");
        }*/
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long startTime = System.currentTimeMillis();
        System.err.println(request.getMethod());
        String originHeader = request.getHeader("Origin");
        originHeader = originHeader == null ? "" : originHeader;

        JSONObject jsonReturn = null;
        JSONObject jsonReceive = null;
        String classId = "";
        String perFixDes = "";
        Object uid = null;
        Object xlsFileName = null;
        Object classType = null;
        ClassTreeInfo classInfo;
        try {
            jsonReturn = JSONObject.parseObject("{}");
            jsonReceive = JSONObject.parseObject(Tools.getStringFromRequest(request));
            HttpSession session = request.getSession();
            if (session == null) {
                jsonReturn.put("result", 2);
                jsonReturn.put("errMsg", "请先登录");
                Tools.sendResponseText(response, originHeader, jsonReturn.toString());
                LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", jsonReturn.toString(), null, null);
                return;
            }
            uid = session.getAttribute("uid");
            xlsFileName = session.getAttribute("filename");
            classType = session.getAttribute("classType");

            classInfo = (ClassTreeInfo) session.getAttribute("classInfo");
            if (uid == null || xlsFileName == null || classInfo == null || classType == null) {
                jsonReturn.put("result", 2);
                jsonReturn.put("errMsg", "参数信息不完整");
                System.err.println("第二次上传被拦截");
                Tools.sendResponseText(response, originHeader, jsonReturn.toString());
                LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", jsonReturn.toString(), null, null);
                return;
            } else {
            }
            classId = classInfo.getRealClassId();
            perFixDes = classInfo.getPerFixDes();
            if (xlsFileName.toString().isEmpty()) {
                jsonReturn.put("result", 2);
                jsonReturn.put("errMsg", "参数信息不完整");
                Tools.sendResponseText(response, originHeader, jsonReturn.toString());
                LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", jsonReturn.toString(), null, null);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonReturn.put("result", 2);
            jsonReturn.put("errMsg", "参数信息不完整");
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", jsonReturn.toString(), null, null);
            return;
        }

        CFilesImport CFilesImport = null;
        boolean canCommit = true;
        ClassTableBiz classTable = null;
        String tableName = "";
        try {
            switch (classType.toString()) {
                case "0":
                    tableName = classInfo.getDocTable();
                    classTable = new ClassTableDocImpl(classId, tableName, perFixDes);
                    break;
                case "1":
                    tableName = classInfo.getBoxTable();
                    classTable = new ClassTableBoxImpl(classId, tableName, perFixDes);
                    break;
                case "2":
                    tableName = classInfo.getRoolTable();
                    classTable = new ClassTableRoolImpl(classId, tableName, perFixDes);
                    break;
                default:
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonReturn.put("errMsg", "查询出错");
            jsonReturn.put("result", 2);
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", e.getMessage(), null, jsonReturn.toString());
        }
        JSONObject assFields;
        String sourceKeyField;
        int mode = -1;
        if (jsonReceive.containsKey("assfields") && jsonReceive.containsKey("keyfield") && jsonReceive.containsKey("mode")) {
            assFields = jsonReceive.getJSONObject("assfields");
            sourceKeyField = jsonReceive.getString("keyfield");
            mode = Integer.parseInt(jsonReceive.getString("mode"));
            if (!assFields.isEmpty() && !sourceKeyField.isEmpty() && mode != -1) {

            } else {
                jsonReturn.put("result", 1);
                jsonReturn.put("errMsg", "数据参数不全");
                Tools.sendResponseText(response, originHeader, jsonReturn.toString());
                LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", jsonReturn.toString(), null, null);
                return;
            }
        } else {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "数据参数不全");
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", jsonReturn.toString(), null, null);
            return;
        }
            /*JSONObject assFields = jsonReceive.getJSONObject("assfields");
            String sourceKeyField = jsonReceive.getString("keyfield");
            int mode = 1;*/
        /*=======================修改代码=============================*/
        //判断当前属于哪种类别 ，拿到其对应的主键ID字段名  （盒级BOXID，文件级DOCID，案卷级ROOLID，其他）
        String targetKeyField = classTable.getKeyFieldName().toUpperCase();
        JSONObject keyFields = JSONObject.parseObject("{}");
        //这里sourceKeyField 传的值没有任何作用
        keyFields.put(targetKeyField, sourceKeyField);

        /*=======================修改代码=============================*/
        //强制字段 （必须要保存的条目字段）
        List<String> MandatoryField = new ArrayList<>();
        MandatoryField.add("DOCNO");
        MandatoryField.add(targetKeyField);

        //初始化就判断档号生成项是否完整,不完整直接会抛异常出来
        try {
            CFilesImport = new CFilesImport(classTable, targetKeyField, xlsFileName.toString(), "..\\..\\TempImportFile\\", assFields, tableName, classType.toString(), perFixDes, MandatoryField);
            StartImportThread sit = new StartImportThread(mode, CFilesImport, uid, keyFields);
            sit.start();
            String ret = "{\"result\":0}";
            jsonReturn = JSONObject.parseObject(ret);
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.DRGL, "导入条目", null, jsonReturn.toString());
            return;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "导入数据初始化失败原因:" + e.getMessage());
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", e.getMessage(), null, jsonReturn.toString());
            return;
        } finally {
            // todo 待确定
            request.getSession().removeAttribute("filename");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(404);
    }
}
