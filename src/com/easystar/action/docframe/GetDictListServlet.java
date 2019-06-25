package com.easystar.action.docframe;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.consts.EasyStarConsts;
import com.tskj.core.system.utility.Tools;
import com.tskj.docframe.dao.DictManager;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.user.dao.UserInfo;
import com.tskj.user.userRightServiceImpl.ContentPowerServiceImpl;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * @author LeonSu
 */
@WebServlet(name = "GetDictListServlet", urlPatterns = "/GetDictList.do")
public class GetDictListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jsonObject = JSONObject.parseObject("{}");
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }
        SessionDataBiz sessionDataImpl = null;
        String classId = "";
        String roleId = "";
        String userId = "";
        ClassTreeInfo classTreeInfo = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
            classTreeInfo = sessionDataImpl.getClassTreeInfo();
            UserInfo userInfo = sessionDataImpl.getUserInfo();
            classId = classTreeInfo.getRealClassId();
            roleId = userInfo.getRoleId();
            userId = userInfo.getUserId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //HttpSession session = request.getSession();
        //ClassTreeInfo classTreeInfo = (ClassTreeInfo) session.getAttribute("classInfo");

        if (sessionDataImpl.getClassType() == null) {
            jsonObject.put("code", 1);
            jsonObject.put("errMsg", "未找到档案库类型");
            Tools.sendResponseText(response, jsonObject.toJSONString());
            LogUtil.info(sessionDataImpl, logModuleConsts.ZDGL, "获取档案库所用的字典", null, jsonObject.toString());
            return;
        }
        String classType = sessionDataImpl.getClassType();


        String tableName = "";
        switch (classType) {
            case EasyStarConsts.DOC:
                tableName = classTreeInfo.getDocTable();
                break;
            case EasyStarConsts.BOX:
                tableName = classTreeInfo.getBoxTable();
                break;
            case EasyStarConsts.ROOL:
                tableName = classTreeInfo.getRoolTable();
                break;
            default:
                jsonObject.put("code", 1);
                jsonObject.put("errMsg", "未找到表名");
                Tools.sendResponseText(response, jsonObject.toJSONString());
                LogUtil.info(sessionDataImpl, logModuleConsts.ZDGL, "获取档案库所用的字典", null, jsonObject.toString());
                return;
        }

        DictManager dictManager = new DictManager();
        List<Map<String, Object>> list = dictManager.getDict(tableName);
        jsonObject.put("code", 0);
        JSONObject dictInfo;
        JSONArray dicts;
        JSONObject allDict = JSONObject.parseObject("{}");
        for (Map<String, Object> map : list) {
            String fieldName = map.get("FIELDNAME").toString().trim();
            if (allDict.containsKey(fieldName)) {
                dictInfo = JSONObject.parseObject("{}");
                dictInfo.put("DICTID", map.get("DICTID").toString().trim());
                dictInfo.put("DVALUE", map.get("DVALUE").toString().trim());
                allDict.getJSONArray(fieldName).add(dictInfo);
            } else {
                dicts = JSONArray.parseArray("[]");
                dictInfo = JSONObject.parseObject("{}");
                dictInfo.put("DICTID", map.get("DICTID").toString().trim());
                dictInfo.put("DVALUE", map.get("DVALUE").toString().trim());
                dicts.add(dictInfo);
                allDict.put(fieldName, dicts);
            }
        }
        //allDict.remove();
        //System.err.println(allDict);
        ContentPowerServiceImpl contentPower = new ContentPowerServiceImpl();
        //classId
        //roleId
        //System.err.println(allDict);
        JSONObject jsonObject1 = contentPower.FindPowerDict(userId, classId, roleId, allDict);

        jsonObject.put("list", jsonObject1);

        //jsonObject.put("list", allDict);
        Tools.sendResponseText(response, jsonObject.toJSONString());
        LogUtil.info(sessionDataImpl, logModuleConsts.ZDGL, "获取档案库所用的字典", null, jsonObject.toString());
    }
}
