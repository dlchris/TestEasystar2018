/**
 * ywzd
 * com.Interceptor
 * CheckLoginInterceptor.java
 * 2017年5月8日
 * 上午10:47:13
 * yu_ta
 */
package com.tskj.core.Interceptor;


import com.tskj.log.util.LogUtil;
import com.tskj.log.util.logModuleConsts;
import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;
import com.tskj.user.dao.UserInfo;
import sun.util.calendar.BaseCalendar;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author JRX
 * @Description:
 * @create 2019/4/11 16:16
 * @return
 **/
//@WebFilter(asyncSupported = true)
public class CheckLoginInterceptor implements Filter {
    public FilterConfig config;


    // 初始化
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.err.println("过滤器启动!!!");
        config = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String includeStrings = config.getInitParameter("includeStrings");    // 过滤资源后缀参数
        //System.err.println(includeStrings);
        //System.err.println(request.getRequestURI().indexOf(includeStrings));
        String requestURI = request.getRequestURI();
        //System.err.println("路径:"+requestURI);
        String urlPath = request.getServletPath();
        //System.err.println("地址:"+urlPath);
        if (requestURI.indexOf(includeStrings) < 0) {// 只对指定过滤参数后缀进行过滤
            filterChain.doFilter(request, response);
        } else {
            Boolean flag = false;
            //过滤字段、路径。。。
            String[] notFilter = new String[]{
                    "Login.do",
                    "UploadMessageServlet.do",//上传
                    "FileHangUpAction.do",// 挂接
                    "DelFileEntityAction.do",//删除原件
                    "GetClassIdAction.do",//获取classId
                    "ClassTreeStatisticsServlet.do",//统计 临时
                    //"GetClassTree.do",
                    //"roleClassTree.do",//角色门类 临时
                    //"RoleFun.do",//角色功能 临时
                    //"RoleFunAssgin.do",//角色功能 临时
                    //"UserManager.do", //用户管理 临时
                    //"RoleBoundUsers.do",//角色绑用户 临时
                    //"SysLogs.do",// 日志  临时
                    //"UserFunction.do",// 用户功能  临时
                    //"UserClassTree.do",// 用户门类  临时
                    //"UserBoundRole.do",// 用户绑定角色  临时
                    "js", "xml", "css", "demo", "img", "images", "fonts", "common"};
            for (String url : notFilter) {
                if (requestURI.contains(url)) {
                    System.err.println("跳过的地址:" + url);
                    flag = true;
                }
            }
            if (flag) {//登录直接跳过拦截
                //System.err.println("跳过");
                filterChain.doFilter(req, res);
            } else {
                //登录成功将登录ID放入session中，这里将session取出对比
              /*  HttpSession session = request.getSession();
                Object userInfo = session.getAttribute("userInfo");
                if (null == userInfo || "".equals(userInfo)) {
                    response.setStatus(401);
                    return;
                } else {//跳过
                    filterChain.doFilter(req, res);
                }*/
                try {
                    SessionDataBiz sessionDataImpl = PermanentDataSourceFactory.getSessionDataImpl(request);
                    if (null == sessionDataImpl) {
                        response.setStatus(401);
                        return;
                    } else {
                        UserInfo userInfo = sessionDataImpl.getUserInfo();
                        String prop = userInfo.getProp();
                        if (!propPower(prop, requestURI)) {
                            LogUtil.error(sessionDataImpl, logModuleConsts.YCGL, "非法访问", "异常调用", null, null);
                            response.setStatus(401);
                            return;
                        }
                        filterChain.doFilter(req, res);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(401);
                    return;
                }


            }
        }
    }

    public boolean propPower(String prop, String requestURI) {

        /*switch (prop) {
            case "0"://操作员

            case "1"://系统管理员

            case "2"://审计员

            default:

        }*/
        //日志类
        String[] logFilter = new String[]{"SysLogs.do"};
        for (String s : logFilter) {
            if (requestURI.contains(s)) {
                if (prop.equals("1")) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        //系统管理类
        String[] sysFilter = new String[]{
                "UserManager.do",
                "RoleManagerServlet.do",
                "RoleNameExist.do",
                "RoleBoundUsers.do",
                "roleClassTree.do",
                "GetClassTree.do",
                "RoleFunAssgin.do",
                "RoleFun.do",
                "UserFunction.do",
                "UserClassTree.do",
                "UserBoundRole.do"
        };

        for (String s : sysFilter) {
            if (requestURI.contains(s)) {
                if (prop.equals("2")) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    //销毁
    @Override
    public void destroy() {
        this.config = null;
    }
}

