package com.tskj.log.thread;

import com.tskj.log.dao.SysLogsDao;
import com.tskj.log.dao.impl.SysLogsDaoImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-05-31 14:53
 **/
public class SaveLogInfoPool implements Runnable {
    private Map<String, Object> logMap;

    public SaveLogInfoPool(Map<String, Object> logMap) {
        this.logMap = logMap;
    }

    @Override
    public void run() {
        List<Map<String, Object>> list = new ArrayList<>();
        SysLogsDao sld = new SysLogsDaoImpl();
        list.add(logMap);
        //System.err.println(list);
        int i = sld.saveLog(list);
        //System.err.println("日志添加:" + (i == 0 ? "成功" : "失败"));
    }
}
