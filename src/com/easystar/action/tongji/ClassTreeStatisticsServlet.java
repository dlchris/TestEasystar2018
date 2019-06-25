package com.easystar.action.tongji;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.biz.ClassTableBiz;
import com.tskj.classtable.biz.ClassTableImpl;
import com.tskj.classtable.impl.ClassTableBoxImpl;
import com.tskj.classtable.impl.ClassTableDocImpl;
import com.tskj.classtable.impl.ClassTableRoolImpl;
import com.tskj.classtable.search.condition.SearchCondition;
import com.tskj.classtable.statistics.impl.ClassTableStatistics;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.classtree.dao.ClassTree;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.user.dao.UserInfo;
import com.tskj.user.userRightServiceImpl.ContentPowerServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-06-18 13:08
 **/
@WebServlet(name = "ClassTreeStatisticsServlet", urlPatterns = "/ClassTreeStatisticsServlet.do")

public class ClassTreeStatisticsServlet extends HttpServlet {
    //统计门类数据
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        JSONObject jsonObject = JSONObject.parseObject("{}");
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }
        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        SessionDataBiz sessionDataImpl = null;
        ClassTableImpl classTable;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
            ClassTreeInfo classTreeInfo = sessionDataImpl.getClassTreeInfo();
            String classId = classTreeInfo.getRealClassId();
            //String classId = "7114C8BDC95041119DFE7226F525CC94";
            ClassTree ct = new ClassTree();
            List<Map<String, Object>> classTree = ct.getClassTree(classId);
            if (classTree != null && !classTree.isEmpty()) {
                String classtype = Tools.toString(classTree.get(0).get("CLASSTYPE"));
                System.err.println(classtype);

                System.err.println(classId);
                String tableName = "";
                String classType = "";
                //DOCTABLE, BOXTABLE, ROOLTABLE
              /*  switch (classtype) {
                    case "0":
                        //tableName = classTreeInfo.getDocTable();
                        tableName = Tools.toString(classTree.get(0).get("DOCTABLE"));
                        break;
                    case "1":
                        //tableName = classTreeInfo.getBoxTable();
                        tableName = Tools.toString(classTree.get(0).get("BOXTABLE"));
                        break;
                    case "2":
                        //tableName = classTreeInfo.getRoolTable();
                        tableName = Tools.toString(classTree.get(0).get("ROOLTABLE"));
                       break;
                    default:
                        jsonObject.put("code", 1);
                        jsonObject.put("errMsg", "档案库信息不正确");
                        //LogUtil.info(sessionDataImpl, logModuleConsts.BBGL, "统计报表", null, jsonObject.toString());
                        return;
                }*/
                tableName = Tools.toString(classTree.get(0).get("DOCTABLE"));
                //String tableName = "";
                //tableName = "DOCUMENT88050C556E4E4988";

                ClassTableStatistics cts = new ClassTableStatistics(tableName);
                String year = jsonGet.getString("year");
                String sql = "";
                if (year != null && !year.isEmpty() && !"-1".equals(year)) {
                    sql = year;
                }
                System.err.println(sql);
                List<Map<String, Object>> data1 = new ArrayList<>();
                List<Map<String, Object>> data2 = new ArrayList<>();
                if (cts.findDeptCount(sql)) {
                    data1 = cts.formatListMap(cts.getData());
                    System.err.println(data1);
                }
                if (cts.findYearDate(sql)) {
                    data2 = cts.formatListMap(cts.getData());
                    System.err.println(data2);
                }
                jsonObject.put("code", 0);
                //机构占比
                jsonObject.put("DEPARTMENT", data1);
                //保管期限占比
                jsonObject.put("SAVEDATE", data2);
            } else {
                jsonObject.put("code", 1);
                jsonObject.put("errMsg", "档案库信息没获取到");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", e.getMessage());
        } finally {
            Tools.sendResponseText(response, jsonObject.toString());
            //LogUtil.info(sessionDataImpl, logModuleConsts.BBGL, "统计报表", null, jsonObject.toString());
        }


    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
