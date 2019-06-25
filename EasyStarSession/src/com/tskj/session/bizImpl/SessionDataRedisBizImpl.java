package com.tskj.session.bizImpl;


import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.user.dao.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @notes: 持久数据存在在redis中 通过sessionID获取到
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-25 13:44
 **/
public class SessionDataRedisBizImpl implements SessionDataBiz {
    private HttpSession session;

    public SessionDataRedisBizImpl(HttpServletRequest request) throws Exception {
        session = request.getSession();

        if (session != null) {
            //通过UID找redis中的持久化信息数据。。。
            Object uid = session.getAttribute("uid");
        } else {
            throw new Exception("SESSION失效");
        }
    }


    @Override
    public UserInfo getUserInfo() {
        return null;
    }

    @Override
    public ClassTreeInfo getClassTreeInfo() {
        return null;
    }

    @Override
    public String getUid() {
        return null;
    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public String getClassId() {
        return "";
    }

    @Override
    public String getClassType() {
        return null;
    }

    @Override
    public String getIp() {
        return null;
    }

    @Override
    public String getMethod() {
        return null;
    }

    @Override
    public String getRequestUri() {
        return null;
    }
}
