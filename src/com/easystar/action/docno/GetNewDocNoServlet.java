package com.easystar.action.docno;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.impl.ClassTableBoxImpl;
import com.tskj.classtable.impl.ClassTableDocImpl;
import com.tskj.classtable.impl.ClassTableRoolImpl;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.db.DbUtility;
import com.tskj.core.system.utility.Tools;
import com.tskj.docno.dao.DocNoDao;
import com.tskj.docno.impl.DocNoEngine;
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

/***
 * @author LeonSu
 */
@WebServlet(name = "GetNewDocNoServlet", urlPatterns = "/GetNewDocNo.do")
public class GetNewDocNoServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonRet = JSONObject.parseObject("{}");

//        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
//            return;
//        }
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        //HttpSession session = request.getSession();
        //ClassTreeInfo classTreeInfo = (ClassTreeInfo) session.getAttribute("classInfo");
        //String classId = classTreeInfo.getRealClassId();
        //String classType = session.getAttribute("classType").toString();
        //System.out.println("classType = " + classType);
        ClassTreeInfo classTreeInfo = sessionDataImpl.getClassTreeInfo();
        String classId = classTreeInfo.getRealClassId();
        String classType = sessionDataImpl.getClassType();
        String tableName = "";
        String typeId = "";
        switch (classType) {
            case "0":
                tableName = classTreeInfo.getDocTable();
                typeId = "docId";
                break;
            case "1":
                tableName = classTreeInfo.getBoxTable();
                typeId = "boxId";
                break;
            case "2":
                tableName = classTreeInfo.getRoolTable();
                typeId = "roolId";
                break;
            default:

                break;
        }
//        String classType = jsonGet.getString("classType");

        DocNoDao docNoDao = new DocNoDao();
        String preFixDes = docNoDao.getPerfixDes(classId);
        DocNoEngine docNoEngine = new DocNoEngine(tableName, classType, preFixDes);
        //docId

        String newOrOld = Tools.toString(jsonGet.get("newOrOld"));
        String docId = "";
        String newDocNo = docNoEngine.getNewDocNoByJson(jsonGet.getJSONObject("values"));

        //档号为空不做验证
        if (!"".equals(newDocNo)) {
            if ("0".equals(newOrOld)) {//新增
                String jsonStr = addDocNo(tableName, newDocNo);
                Tools.sendResponseText(response, "", jsonStr);
            } else if ("1".equals(newOrOld)) {//修改
                docId = Tools.toString(jsonGet.get("docId"));
                String jsonStr = UpdateDocNo(tableName, newDocNo, docId, typeId);
                Tools.sendResponseText(response, "", jsonStr);
            } else {
                jsonRet.put("code", 0);
                jsonRet.put("docNo", newDocNo);
                Tools.sendResponseText(response, "", jsonRet.toJSONString());
            }
        } else {
            jsonRet.put("code", 0);
            jsonRet.put("docNo", newDocNo);
            Tools.sendResponseText(response, "", jsonRet.toJSONString());
        }
        LogUtil.info(sessionDataImpl, logModuleConsts.DHGL, "实时生成档号", null, jsonRet.toString());
    }


    public String addDocNo(String tableName, String newDocNo) {
        JSONObject jsonRet = JSONObject.parseObject("{}");
        String sqlStr = "";
        jsonRet.put("docNo", newDocNo);
        String sql = "SELECT COUNT(*) AS count FROM " + tableName + " where docNo = '" + newDocNo + "'";
        System.err.println(sql);
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list.size() > 0) {
            String count = list.get(0).get("count").toString();
            int a = Integer.parseInt(count);
            jsonRet.put("code", 0);
            jsonRet.put("docNo", newDocNo);
            if (a > 0) {
                jsonRet.put("code", 1);
                jsonRet.put("errMsg", "档号重复");
                //return jsonRet.toString();
            } else {
                jsonRet.put("code", 0);
            }
        } else {
            jsonRet.put("code", 1);
            jsonRet.put("errMsg", "档号查重接口异常");
        }
        return jsonRet.toString();
    }

    public String UpdateDocNo(String tableName, String newDocNo, String docId, String typeId) {
        JSONObject jsonRet = JSONObject.parseObject("{}");
        jsonRet.put("docNo", newDocNo);
        String sqlStr = "";
        String sql = "SELECT COUNT(*) AS count FROM " + tableName + " where " + typeId + " != '" + docId + "' and docNo = '" + newDocNo + "'";
        System.err.println(sql);
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list.size() > 0) {
            String count = list.get(0).get("count").toString();
            int a = Integer.parseInt(count);
            if (a > 0) {
                jsonRet.put("code", 1);
                jsonRet.put("errMsg", "档号重复");
            } else {
                jsonRet.put("code", 0);
            }
        } else {
            jsonRet.put("code", 1);
            jsonRet.put("errMsg", "档号查重接口异常");
        }
        return jsonRet.toJSONString();
    }

    public static void main(String[] args) {
        //json str = {values:{ALLNO:"0020"},docId:"123",newOrOld:"0"}
      /*  ALLNO	0020
        CLASSTYPE	文书
        CLASSTYPE_ID	WS
        DEPARTMENT	经协办
        DEPARTMENT_ID	007
        FIELD0B546940487B4AAA	30年
        FIELD0B546940487B4AAA_ID	D30
        NOTENO	0068
        YEARNO	0036*/

    }

    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(404);
    }
}
