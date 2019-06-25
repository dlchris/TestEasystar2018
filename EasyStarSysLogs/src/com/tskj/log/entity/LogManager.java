package com.tskj.log.entity;

import com.tskj.session.biz.SessionDataBiz;
import com.tskj.session.bizImpl.PermanentDataSourceFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-06-04 09:55
 **/
public class LogManager {
    private String userName;//用户名
    private String moduleName;//模块名称
    private String functionName;//功能名称
    private String sysTime;//操作时间
    private String sysMemo;//备注
    private String OPERATETIME;//操作时间
    private String IP;//IP
    private String EXCEPTION;//报错
    private String REQUESTURL;//请求地址
    private String TYPE;//日志类型 (info正常返回,error后台错误500空指针等)
    private String METHOD;//日志类型 (info正常返回,error后台错误500空指针等)

    public LogManager(SessionDataBiz sysData, String moduleName, String functionName, String sysMemo) {
        //com.tskj.classtree.bean.ClassTreeInfo classTreeInfo = sysData.getClassTreeInfo();
        Date date = new Date();
        this.userName = sysData.getUserInfo().getUserName();
        this.moduleName = moduleName;
        this.functionName = functionName;
        this.sysMemo = sysMemo;
        this.OPERATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        this.IP = sysData.getIp();
        this.METHOD = sysData.getMethod();
        this.REQUESTURL = sysData.getRequestUri();
        this.TYPE = "info";
    }

    /*public LogManager(String userName, String moduleName, String functionName, String sysMemo) {
        Date date = new Date();
        this.userName = userName;
        this.moduleName = moduleName;
        this.functionName = functionName;
        this.sysTime = new SimpleDateFormat("yyyy-MM-dd").format(date);
        this.sysMemo = sysMemo;


        //this.sysMemo = "操作时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + sysMemo;
    }*/

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getSysTime() {
        return sysTime;
    }

    public void setSysTime(String sysTime) {
        this.sysTime = sysTime;
    }

    public String getSysMemo() {
        return sysMemo;
    }

    public void setSysMemo(String sysMemo) {
        this.sysMemo = sysMemo;
    }
}
