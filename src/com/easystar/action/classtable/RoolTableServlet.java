package com.easystar.action.classtable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.biz.ClassTableBiz;
import com.tskj.classtable.impl.*;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.utility.Tools;
import com.tskj.docframe.dao.DocFrameManager;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.user.dao.UserInfo;
import com.tskj.user.userRightService.ContentPowerService;
import com.tskj.user.userRightServiceImpl.ContentPowerServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LeonSu
 */
@WebServlet(name = "RoolTableServlet", urlPatterns = "/RoolTable.do")
public class RoolTableServlet extends HttpServlet {
    private ClassTableBiz classTable = null;
    private ContentPowerService contentPower = null;
    private String classId;
    private String tableName;
    private String perFixDes;


    /**
     * 创建案卷级条目
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }

        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        SessionDataBiz sessionDataImpl = null;
        JSONObject jsonObject = JSONObject.parseObject("{}");
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", e.getMessage());
        }
        ClassTreeInfo classInfo = sessionDataImpl.getClassTreeInfo();
        /*HttpSession session = request.getSession();
        ClassTreeInfo classInfo = (ClassTreeInfo) session.getAttribute("classInfo");*/
        classId = classInfo.getRealClassId();
        tableName = classInfo.getRoolTable();
        perFixDes = classInfo.getPerFixDes();

        try {
            classTable = new ClassTableRoolImpl(classId, tableName, perFixDes);
            JSONObject value = jsonGet.getJSONObject("data");
            jsonObject = classTable.save(value);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", e.getMessage());
        }
        LogUtil.info(sessionDataImpl, logModuleConsts.MLGL, "创建案卷级条目", null, jsonObject.toString());
        Tools.sendResponseText(response, jsonObject.toString());

    }

    /**
     * 读取案卷级条目
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }
        SessionDataBiz sessionDataImpl = null;
        JSONObject jsonObject = JSONObject.parseObject("{}");
        String errMsg = "";

        int pageSize = Integer.valueOf(request.getParameter("pageSize"));
        int pageIndex = Integer.valueOf(request.getParameter("pageIndex"));
        try {
            //DISSTR as 'label', FIELDNAME as 'prop', 'true' as 'overHidden', FIELDSIZE, FIELDTYPE
            /*HttpSession session = request.getSession();
            ClassTreeInfo classInfo = (ClassTreeInfo) session.getAttribute("classInfo");
            classId = classInfo.getRealClassId();
            tableName = classInfo.getRoolTable();
            perFixDes = classInfo.getPerFixDes();*/
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
            ClassTreeInfo classInfo = sessionDataImpl.getClassTreeInfo();
            UserInfo userInfo = sessionDataImpl.getUserInfo();
            classId = classInfo.getRealClassId();
            tableName = classInfo.getRoolTable();
            perFixDes = classInfo.getPerFixDes();
            String userId = userInfo.getUserId();
            String roleId = userInfo.getRoleId();

            contentPower = new ContentPowerServiceImpl();
            //获取看到的条目权限条件
            String power = contentPower.ContentAuthority(userId, classId, roleId, "ROOLID");
            //System.err.println("权限条件:"+power);
            Map<String, Object> params = new HashMap<>();
            params.put("power", power);

            classTable = new ClassTableRoolImpl(classId, tableName, perFixDes);

            List<Map<String, Object>> datas = classTable.getData(params, pageSize, pageIndex, "DOCNO", "DESC");
            int size = classTable.getSize(power);
            jsonObject.put("code", 0);
            jsonObject.put("cols", classTable.getFields());
            jsonObject.put("list", datas);
            jsonObject.put("count", size);
            jsonObject.put("style", "page");//样式 page 分页, nopage 不分页
        } catch (SQLException e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", "Get field failure");
            errMsg = e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", e.getMessage());
            errMsg = e.getMessage();
        }
        Tools.sendResponseText(response, jsonObject.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "读取案卷级条目", errMsg, null);
    }

    /**
     * 更新案卷级条目
     *
     * @param request
     * @param response
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    /**
     * 删除案卷级条目
     *
     * @param request
     * @param response
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }

        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));

        SessionDataBiz sessionDataImpl = null;
        JSONObject jsonObject = JSONObject.parseObject("{}");
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", e.getMessage());
        }
        ClassTreeInfo classInfo = sessionDataImpl.getClassTreeInfo();
        /*HttpSession session = request.getSession();
        ClassTreeInfo classInfo = (ClassTreeInfo) session.getAttribute("classInfo");*/

        if (classInfo.getRealClassId().isEmpty()) {
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", "缺少档案库ID");
        } else {
            String flag = jsonGet.getString("flag");
            JSONArray value = jsonGet.getJSONArray("data");
            classId = classInfo.getRealClassId();
            tableName = classInfo.getRoolTable();
            perFixDes = classInfo.getPerFixDes();
            try {
                classTable = new ClassTableRoolImpl(classId, tableName, perFixDes);
                if ("0".equals(flag)) {      //批量删除
                    jsonObject = classTable.mutilDel(value);

                } else if ("1".equals(flag)) {    //单个删除
                    jsonObject = classTable.singleDel(value.getJSONObject(0));

                } else {
                    jsonObject.put("code", 1);
                    jsonObject.put("errMsg", "参数有误！");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                jsonObject.put("code", 1);
                jsonObject.put("errMsg", e.getMessage());
            }
        }
        Tools.sendResponseText(response, jsonObject.toString());
        LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "删除案卷级条目", null, jsonObject.toString());

    }
}
