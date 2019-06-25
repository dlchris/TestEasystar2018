package com.tskj.session.bizImpl;

import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.utility.Tools;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.user.dao.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @notes: 持久化数据源选择
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-25 13:38
 **/
public class PermanentDataSourceFactory {

    public static SessionDataBiz getSessionDataImpl(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        //System.err.println(session.getId());
        if (session != null) {
            String perType = Tools.toString(session.getAttribute("perType"));
            switch (perType.toUpperCase().trim()) {
                case "SESSION":
                    return new SessionDataBizImpl(request);
                case "REDIS":
                    return new SessionDataRedisBizImpl(request);
                default:
                    return null;
            }
        } else {
            throw new Exception("SESSION失效");
        }
    }

    public static boolean removeSessionDataImpl(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            return true;
        } else {
            return true;
        }
    }

}
