package com.easystar.action.classtable;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.biz.ClassTableImpl;
import com.tskj.classtable.impl.ClassTableBoxImpl;
import com.tskj.classtable.impl.ClassTableDocImpl;
import com.tskj.classtable.impl.ClassTableRoolImpl;
import com.tskj.classtable.search.condition.SearchCondition;
import com.tskj.classtree.bean.ClassTreeInfo;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author LeonSu
 */
@WebServlet(name = "ClassTableSearchServlet", urlPatterns = "/Search.do")
public class ClassTableSearchServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jsonObject = JSONObject.parseObject("{}");
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
       /* if (session.getAttribute("classType") == null || session.getAttribute("classInfo") == null) {
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", "没有档案库信息");
            Tools.sendResponseText(response, jsonObject.toString());
            return;
        }*/

        JSONObject jsonGet = JSONObject.parseObject(Tools.getStringFromRequest(request));
        SessionDataBiz sessionDataImpl = null;
        //ClassTreeInfo classTreeInfo = (ClassTreeInfo) session.getAttribute("classInfo");
        ClassTableImpl classTable;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
            ClassTreeInfo classTreeInfo = sessionDataImpl.getClassTreeInfo();
            String classType = "";
            switch (sessionDataImpl.getClassType().toString()) {

                case "0":
                    classTable = new ClassTableDocImpl(classTreeInfo.getRealClassId(), classTreeInfo.getDocTable(), classTreeInfo.getPerFixDes());
                    classType = "DOCID";
                    break;
                case "1":
                    classTable = new ClassTableBoxImpl(classTreeInfo.getRealClassId(), classTreeInfo.getBoxTable(), classTreeInfo.getPerFixDes());
                    classType = "BOXID";
                    break;
                case "2":
                    classTable = new ClassTableRoolImpl(classTreeInfo.getRealClassId(), classTreeInfo.getRoolTable(), classTreeInfo.getPerFixDes());
                    classType = "ROOLID";
                    break;
                default:
                    jsonObject.put("code", 1);
                    jsonObject.put("errMsg", "档案库信息不正确");
                    LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "精确查询", null, jsonObject.toString());
                    return;
            }


            //SessionDataBiz sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
            //ClassTreeInfo classInfo = sessionDataImpl.getClassTreeInfo();
            UserInfo userInfo = sessionDataImpl.getUserInfo();
            String classId = classTreeInfo.getRealClassId();
            String userId = userInfo.getUserId();
            String roleId = userInfo.getRoleId();

            ContentPowerServiceImpl contentPower = new ContentPowerServiceImpl();
            //获取看到的条目权限条件
            String power = contentPower.ContentAuthority(userId, classId, roleId, classType);
            //System.err.println("权限条件:" + power);


            SearchCondition searchCondition = new SearchCondition();
            String whereStr = searchCondition.getString(jsonGet.getJSONArray("data"));
            classTable.search(whereStr, "DOCNO", power, classType);

            jsonObject.put("code", 0);
            jsonObject.put("cols", classTable.getFields());
            jsonObject.put("list", classTable.getData());
            jsonObject.put("count", classTable.getData().size());
            jsonObject.put("style", "nopage");//样式 page 分页, nopage 不分页
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", e.getMessage());
        } finally {
            Tools.sendResponseText(response, jsonObject.toString());
            LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "精确查询", null, jsonObject.toString());
        }
    }

}
