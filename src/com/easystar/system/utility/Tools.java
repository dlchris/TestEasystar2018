package com.easystar.system.utility;

import com.alibaba.fastjson.JSONObject;
import com.easystar.system.classtree.ClassTree;
import com.tskj.user.dao.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Tools {
    public static boolean checkSession(HttpServletRequest request, HttpServletResponse response) {
        return true;
//        JSONObject jsonObject = JSONObject.parseObject("{}");
//
//        HttpSession session = request.getSession(false);
//        if (session == null || session.getAttribute("userInfo") == null) {
//            jsonObject.put("code", 1);
//            jsonObject.put("errMsg", "请重新登录");
//            com.tskj.core.system.utility.Tools.sendResponseText(response, "", jsonObject.toString());
//            return false;
//        }
//        return true;
    }

//    /**
//     * 根据门类ID和门类类型，得到实体表名
//     * @param classId
//     * @param classType
//     *  0：文件级；1：盒级；2：案卷级
//     * @return
//     */
//    public static String getTableName(String classId, String classType) {
//        ClassTree classTree = new ClassTree();
//        return classTree.getTableName(classId, classType);
//    }

    public static <T> T getSessionObject(HttpSession session, String attribName, Class<T> clazz) {
        if (session.getAttribute(attribName) != null) {
            return (T) session.getAttribute(attribName);
        }
        return null;
    }
}
