package com.tskj.session.bizImpl;

import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.system.ip.IPUtil;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.user.dao.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-25 13:43
 **/
public class SessionDataBizImpl implements SessionDataBiz {
    private HttpSession session;
    private ClassTreeInfo classTreeInfo;
    private UserInfo userInfo;
    private String ip;
    private String method;
    private String requestUri;

    public SessionDataBizImpl(HttpServletRequest request) throws Exception {
        session = request.getSession();
        ip = IPUtil.getIpAddr(request);
        //得到请求URL地址时使用的方法
        method = request.getMethod();
        requestUri = request.getRequestURI();//得到请求的资源
        String contextPath = request.getContextPath();
        //System.err.println("ip:" + ip);
        //System.err.println("请求方式:" + method);
        //System.err.println("请求接口名:" + requestUri);
        //System.err.println("具体是什么:" + contextPath);
        if (session != null) {
            classTreeInfo = (ClassTreeInfo) session.getAttribute("classInfo");
            userInfo = (UserInfo) session.getAttribute("userInfo");
        } else {
            throw new Exception("SESSION失效");
        }
    }

    @Override
    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    @Override
    public ClassTreeInfo getClassTreeInfo() {
        return this.classTreeInfo;
    }

    @Override
    public String getUid() {
        return toString(session.getAttribute("uid"));
    }

    @Override
    public String getFileName() {
        return toString(session.getAttribute("filename"));
    }

    @Override
    public String getClassId() {
        return this.classTreeInfo.getRealClassId();
    }

    @Override
    public String getClassType() {
        return toString(session.getAttribute("classType"));
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getRequestUri() {
        return requestUri;
    }

    /**
     * 字符串工具
     *
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        return (obj == null ? "" : obj.toString());
    }

}
