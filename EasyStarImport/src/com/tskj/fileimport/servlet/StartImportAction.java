package com.tskj.fileimport.servlet;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.biz.ClassTableBiz;
import com.tskj.classtable.impl.ClassTableBoxImpl;
import com.tskj.classtable.impl.ClassTableDocImpl;
import com.tskj.classtable.impl.ClassTableRoolImpl;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.docframe.dao.DocFrameManager;
import com.tskj.docno.bean.DocNoFieldInfo;
import com.tskj.docno.dao.DocNoDao;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.fileimport.process.DataManager;
import com.tskj.fileimport.process.DataProcess;
import com.tskj.fileimport.system.CFileImport;
import com.tskj.fileimport.system.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author LeonSu
 */
@WebServlet(name = "StartImportAction", urlPatterns = "/StartImportAction.do")
public class StartImportAction extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String originHeader = request.getHeader("Origin");
        originHeader = originHeader == null ? "" : originHeader;

        JSONObject jsonReturn = JSONObject.parseObject("{}");
        JSONObject jsonReceive = JSONObject.parseObject(Tools.getStringFromRequest(request));

        HttpSession session = request.getSession();

        Object uid = session.getAttribute("uid");
        Object xlsFileName = session.getAttribute("filename");

        ClassTreeInfo classInfo = (ClassTreeInfo) session.getAttribute("classInfo");
        String classId = classInfo.getRealClassId();
        String perFixDes = classInfo.getPerFixDes();
        String classType = session.getAttribute("classType").toString();
        if (uid == null || xlsFileName == null || classId == null || classType == null) {
            jsonReturn.put("result", 2);
            jsonReturn.put("errMsg", "请重新上传");
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", jsonReturn.toString(), null, null);
            return;
        }

        if (xlsFileName.toString().isEmpty()) {
            jsonReturn.put("result", 2);
            jsonReturn.put("errMsg", "参数信息不完整");
            Tools.sendResponseText(response, originHeader, jsonReturn.toString());
            LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", jsonReturn.toString(), null, null);
            return;
        }
        CFileImport cFileImport = null;
        boolean canCommit = true;
        ClassTableBiz classTable = null;
        try {
            String tableName = "";
            switch (classType) {
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
            if (!jsonReceive.containsKey("assfields")) {
                jsonReturn.put("result", 1);
                jsonReturn.put("errMsg", "没有对应关系");
                Tools.sendResponseText(response, originHeader, jsonReturn.toString());
                LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", jsonReturn.toString(), null, null);
                return;
            }
            JSONObject assFields = jsonReceive.getJSONObject("assfields");
            String sourceKeyField = jsonReceive.getString("keyfield");

            int mode = Integer.parseInt(jsonReceive.getString("mode"));
            /*=======================修改代码=============================*/
            //判断对应关系中 档号生成项是否完整，不完整直接return
            DocNoDao docNoDao = new DocNoDao();
            String preFixDes = docNoDao.getPerfixDes(classId);
            //DocNoEngine docNoEngine = new DocNoEngine(classType, preFixDes);
            DocNoEngine docNoEngine = new DocNoEngine(tableName, classType, preFixDes);
            Vector<DocNoFieldInfo> docNoRule = docNoEngine.getDocNoRule();
            //档号组成项数量
            int size = docNoRule.size();
            int rel = 0;

            for (String s : assFields.keySet()) {
                //验证是否是档号组成项,是+1
                if (docNoEngine.indexOf(s)) {
                    ++rel;
                }
            }
            if (rel != size) {
                jsonReturn.put("result", 1);
                jsonReturn.put("errMsg", "档号组成项不完全");
                Tools.sendResponseText(response, originHeader, jsonReturn.toString());
                LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", jsonReturn.toString(), null, null);
                return;
            }


            //判断当前属于哪种类别 ，拿到其对应的主键ID字段名  （盒级BOXID，文件级DOCID，案卷级ROOLID，其他）
            //String targetKeyField = new DocFrameManager().getKeyField(classId.toString(), classType.toString());
            String targetKeyField = classTable.getKeyFieldName();
            JSONObject keyFields = JSONObject.parseObject("{}");
            //这里sourceKeyField 传的值没有任何作用
            keyFields.put(targetKeyField, sourceKeyField);

            /*=======================修改代码=============================*/

            cFileImport = new CFileImport(xlsFileName.toString(), "..\\..\\TempImportFile\\");
            //excal中空行之前的数据条数
            int xlsCount = cFileImport.getNotEmptyRowCount();
            canCommit = true;
            String ret = "{\"result\":0}";
            int i;
            DataProcess dataProcess = new DataProcess(uid.toString(), xlsCount);
            DataManager.getInstance().add(dataProcess);

            cFileImport.startTrans();
            System.err.println("多少条数据：" + xlsCount);
            for (i = 1; i <= xlsCount; i++) {
                JSONObject ret1 = cFileImport.save(classTable, mode, tableName, keyFields, assFields, null, i, xlsCount, docNoEngine);
                if (ret1.getInteger("result") != 0) {
                    canCommit = false;
                    dataProcess.addFailure(i, ret1.getString("errMsg"));

                } else {
                    dataProcess.addSuccess();
                }
            }
            dataProcess.setFinished(true);
            jsonReturn = JSONObject.parseObject(ret);
            LogUtil.info(sessionDataImpl, logModuleConsts.DRGL, "导入条目", null, jsonReturn.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtil.error(sessionDataImpl, logModuleConsts.DRGL, "导入条目", e.getMessage(), null, jsonReturn.toString());
        } finally {
            if (cFileImport != null) {
                cFileImport.endTrans(canCommit);
                cFileImport.delFile();
            }
            request.getSession().removeAttribute("filename");
        }
        Tools.sendResponseText(response, originHeader, jsonReturn.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(404);
    }
}
