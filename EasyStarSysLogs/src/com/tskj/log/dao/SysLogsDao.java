package com.tskj.log.dao;

import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-05-30 10:09
 **/
public interface SysLogsDao {

    //日志分页
    List<Map<String, Object>> FindAllSysLogs(int pageIndex, int pageSize);

    //日志总条数
    int countSysLogs();

    //保存日志
    int saveLog(List<Map<String , Object>> params);
}
