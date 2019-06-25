package com.easystar.action.classtable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.easystar.system.consts.ConstsMenu;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.user.dao.UserInfo;
import com.tskj.user.userRightService.UserManageService;
import com.tskj.user.userRightServiceImpl.UserManageServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * 根据档案库类型，得到可操作列表
 *
 * @author LeonSu
 */
@WebServlet(name = "ClassPanelServlet", urlPatterns = "/ClassPanel.do")
public class ClassPanelServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!com.easystar.system.utility.Tools.checkSession(request, response)) {
            return;
        }
        SessionDataBiz sessionDataImpl = null;
        try {
            sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        UserManageService userManage = new UserManageServiceImpl();
        JSONObject jsonRet = JSONObject.parseObject("{}");
        String classID;// = request.getParameter("classId");
        String classType;// = request.getParameter("classType");

        HttpSession session = request.getSession(false);

        //ClassTreeInfo classInfo = (ClassTreeInfo) session.getAttribute("classInfo");
        ClassTreeInfo classInfo = sessionDataImpl.getClassTreeInfo();
//        classID = classInfo.getRealClassId();
        classType = String.valueOf(classInfo.getClassType());

        //UserInfo userInfo = (UserInfo) session.getAttribute("userInfo");
        UserInfo userInfo = sessionDataImpl.getUserInfo();
        JSONArray menuData = JSONArray.parseArray("[]");

        Map<String, String> menuDatas = userManage.getMenuData(userInfo.getUserId(), userInfo.getRoleId());   //文件级、盒级、案卷级Map集合
        String docMenu = menuDatas.get("docMenu");     //文件级管理菜单
//        String fileMenu = userManage.getFileMenu(userInfo.getUserId(), userInfo.getRoleId());   //原件管理菜单
        String fileMenu = null;       //设置为null，前台不显示原件管理
        // 2019 06-11 注释掉
        //String logMenu = userManage.getLogMenu(userInfo.getUserId(), userInfo.getRoleId());  //日志管理菜单
        boolean fileMenuFlag = false;
        boolean logMenuFlag = false;
        if (fileMenu != null && !"".equals(fileMenu)) {
            fileMenuFlag = true;
        }
        // 2019 06-11 注释掉
        /*if (logMenu != null && !"".equals(logMenu)) {
            logMenuFlag = true;
        }*/

//        System.out.println("类型："+classType);

        switch (classType) {
            case "0":
                if (true) {
//                    menuData.add(JSONObject.parseObject(ConstsMenu.MENU_DOC));
//                    String docMenu = userManage.getDocMenu(userInfo.getUserId(), userInfo.getRoleId());
                    menuData.add(JSONObject.parseObject(docMenu));
                }
                if (fileMenuFlag) {
//                    menuData.add(JSONObject.parseObject(ConstsMenu.MENU_FILES));
                    menuData.add(JSONObject.parseObject(fileMenu));
                }
//                menuData.add(JSONObject.parseObject(ConstsMenu.MENU_LOG));
                // 2019 06-11 注释掉
               /* if (logMenuFlag) {
                    menuData.add(JSONObject.parseObject(logMenu));
                }*/
                //无盒无卷
                break;
            case "1":
                //装盒
                /*menuData.add(JSONObject.parseObject(ConstsMenu.MENU_DOC));
                menuData.add(JSONObject.parseObject(ConstsMenu.MENU_BOX));
                menuData.add(JSONObject.parseObject(ConstsMenu.MENU_FILES));
                menuData.add(JSONObject.parseObject(ConstsMenu.MENU_LOG));*/

                menuData.add(JSONObject.parseObject(docMenu));
                menuData.add(JSONObject.parseObject(menuDatas.get("boxMenu")));
                if (fileMenuFlag) {
                    menuData.add(JSONObject.parseObject(fileMenu));
                }
                // 2019 06-11 注释掉
                /*if (logMenuFlag) {
                    menuData.add(JSONObject.parseObject(logMenu));
                }*/
                break;
            case "2":
                //订卷
                /*menuData.add(JSONObject.parseObject(ConstsMenu.MENU_DOC));
                menuData.add(JSONObject.parseObject(ConstsMenu.MENU_ROOL));
                menuData.add(JSONObject.parseObject(ConstsMenu.MENU_FILES));
                menuData.add(JSONObject.parseObject(ConstsMenu.MENU_LOG));*/

                menuData.add(JSONObject.parseObject(docMenu));
                menuData.add(JSONObject.parseObject(menuDatas.get("roolMenu")));
                if (fileMenuFlag) {
                    menuData.add(JSONObject.parseObject(fileMenu));
                }
                // 2019 06-11 注释掉
                /*if (logMenuFlag) {
                    menuData.add(JSONObject.parseObject(logMenu));
                }*/
                break;
            default:
                break;
        }
        jsonRet.put("code", 0);
        jsonRet.put("list", menuData);
        Tools.sendResponseText(response, "", jsonRet.toJSONString());
        LogUtil.info(sessionDataImpl, logModuleConsts.DAGL, "根据档案库类型，得到可操作列表", null, jsonRet.toString());
    }
}
