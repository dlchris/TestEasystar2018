package com.tskj.log.util;

import com.tskj.core.system.ip.IPUtil;
import com.tskj.core.system.utility.Tools;
import com.tskj.log.dao.SysLogsDao;
import com.tskj.log.dao.impl.SysLogsDaoImpl;
import com.tskj.log.thread.SaveLogInfoPool;
import com.tskj.log.thread.ThreadManager;
import com.tskj.session.biz.SessionDataBiz;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-06-04 15:51
 **/
public class LogUtil {

    /**
     * @param sysData      用于获取session和ip等数据
     * @param moduleName   模块名称
     * @param functionName 接口名称
     * @param sysMemo      备注
     * @param jsonSend     返回数据
     * @return
     * @Author JRX
     * @Description:
     * @create 2019/6/4 22:39
     **/

    //正确日志
    public static void info(SessionDataBiz sysData, String moduleName, String functionName, String sysMemo, String jsonSend) {

        Date date = new Date();
        Map<String, Object> logMap = new HashMap<>();
        if (sysData != null) {
            logMap.put("IP", sysData.getIp());
            logMap.put("METHOD", sysData.getMethod());
            logMap.put("REQUESTURL", sysData.getRequestUri());
            logMap.put("USERNAME", sysData.getUserInfo().getUserName());
        }
        logMap.put("LOGID", Tools.newId());
        logMap.put("MODULENAME", moduleName);
        logMap.put("FUNCTIONNAME", functionName);
        logMap.put("SYSMEMO", sysMemo == null ? "" : sysMemo);
        logMap.put("OPERATETIME", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        logMap.put("TYPE", "info");
        logMap.put("JSONSEND", jsonSend == null ? "" : jsonSend);
        ThreadManager.getThreadPool().executor(new SaveLogInfoPool(logMap));
        /*ThreadManager.getThreadPool().executor(new Runnable() {
            @Override
            public void run() {
                SysLogsDao sld = new SysLogsDaoImpl();
                List<Map<String, Object>> list = new ArrayList<>();
                list.add(logMap);
                System.err.println(list);
                sld.saveLog(list);
            }
        });*/
    }

    //错误日志 exception时使用
    public static void error(SessionDataBiz sysData, String moduleName, String functionName, String errMsg, String sysMemo, String jsonSend) {
        Date date = new Date();
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("LOGID", Tools.newId());
        logMap.put("FUNCTIONNAME", functionName);
        logMap.put("MODULENAME", moduleName);
        logMap.put("SYSMEMO", sysMemo == null ? "" : sysMemo);
        logMap.put("OPERATETIME", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        logMap.put("EXCEPTION", errMsg);
        logMap.put("TYPE", "error");
        logMap.put("JSONSEND", jsonSend == null ? "" : jsonSend);
        if (sysData != null) {
            logMap.put("IP", sysData.getIp());
            logMap.put("METHOD", sysData.getMethod());
            logMap.put("REQUESTURL", sysData.getRequestUri());
            logMap.put("USERNAME", sysData.getUserInfo().getUserName());
        }
        ThreadManager.getThreadPool().executor(new SaveLogInfoPool(logMap));
        /*ThreadManager.getThreadPool().executor(new Runnable() {
            @Override
            public void run() {
                List<Map<String, Object>> list = new ArrayList<>();
                SysLogsDao sld = new SysLogsDaoImpl();
                list.add(logMap);
                System.err.println(list);
                sld.saveLog(list);
            }
        });*/
    }

    //无session 报错收集
    public static void emptySErrorLog(HttpServletRequest request, String moduleName, String functionName, String errMsg, String sysMemo, String jsonSend) {
        String Ip = IPUtil.getIpAddr(request);
        Date date = new Date();
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("LOGID", Tools.newId());
        logMap.put("USERNAME", Ip);
        logMap.put("FUNCTIONNAME", functionName);
        logMap.put("SYSMEMO", sysMemo == null ? "" : sysMemo);
        logMap.put("MODULENAME", moduleName);
        logMap.put("OPERATETIME", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        logMap.put("IP", Ip);
        logMap.put("REQUESTURL", request.getRequestURI());
        logMap.put("EXCEPTION", errMsg == null ? "" : errMsg);
        logMap.put("METHOD", request.getMethod());
        logMap.put("TYPE", "error");
        logMap.put("JSONSEND", jsonSend == null ? "" : jsonSend);

        ThreadManager.getThreadPool().executor(new SaveLogInfoPool(logMap));
       /* ThreadManager.getThreadPool().executor(new Runnable() {
            @Override
            public void run() {
                List<Map<String, Object>> list = new ArrayList<>();
                SysLogsDao sld = new SysLogsDaoImpl();
                list.add(logMap);
                System.err.println(list);
                sld.saveLog(list);
            }
        });*/
    }

    //无session
    public static void emptySessionLog(HttpServletRequest request, String moduleName, String functionName, String sysMemo, String jsonSend) {
        String Ip = IPUtil.getIpAddr(request);
        Date date = new Date();
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("LOGID", Tools.newId());
        logMap.put("USERNAME", Ip);
        logMap.put("FUNCTIONNAME", functionName);
        logMap.put("MODULENAME", moduleName);
        logMap.put("SYSMEMO", sysMemo == null ? "" : sysMemo);
        logMap.put("OPERATETIME", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        logMap.put("IP", Ip);
        logMap.put("REQUESTURL", request.getRequestURI());
        logMap.put("METHOD", request.getMethod());
        logMap.put("TYPE", "info");
        logMap.put("JSONSEND", jsonSend == null ? "" : jsonSend);

        ThreadManager.getThreadPool().executor(new SaveLogInfoPool(logMap));
        /*ThreadManager.getThreadPool().executor(new Runnable() {
            @Override
            public void run() {
                List<Map<String, Object>> list = new ArrayList<>();
                SysLogsDao sld = new SysLogsDaoImpl();
                list.add(logMap);
                System.err.println(list);
                sld.saveLog(list);
            }
        });*/
    }

}
