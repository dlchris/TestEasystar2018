package com.tskj.log.dao.impl;

import com.tskj.core.db.DbUtility;
import com.tskj.core.db.dao.DbBasicDao;
import com.tskj.core.db.dao.DbBasicDaoImpl;
import com.tskj.core.system.consts.DBConsts;
import com.tskj.log.dao.SysLogsDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-05-30 10:19
 **/
public class SysLogsDaoImpl implements SysLogsDao {
    private DbBasicDao dbd = new DbBasicDaoImpl();

    @Override
    public List<Map<String, Object>> FindAllSysLogs(int pageIndex, int pageSize) {
        String sql = "";
        List<Map<String, Object>> list = null;
        switch (DbUtility.getDBType()) {
            case DBConsts.DB_MYSQL:
                sql = "select * from logs ORDER BY OPERATETIME DESC";
                list = dbd.MySqlQueryPage(sql, pageSize, pageIndex);
                break;
            case DBConsts.DB_SQLSERVER:
                sql = "select *,ROW_NUMBER() OVER(ORDER BY OPERATETIME DESC) AS rowNum FROM logs";
                list = dbd.MsSqlQuerypage(sql, pageSize, pageIndex);
                break;
            case DBConsts.DB_ORACLE:
            default:
                break;
        }
        return list;
    }

    @Override
    public int countSysLogs() {
        String sql = "select count(*) as num from logs";
        return dbd.TotalRows(sql);
    }

    @Override
    public int saveLog(List<Map<String, Object>> params) {
        List<String> list = new ArrayList<>();
        list.add("LOGID");
        list.add("USERNAME");
        list.add("MODULENAME");
        list.add("FUNCTIONNAME");
        list.add("SYSTIME");
        list.add("SYSMEMO");
        list.add("OPERATETIME");
        list.add("IP");
        list.add("EXCEPTION");
        list.add("REQUESTURL");
        list.add("TYPE");
        list.add("METHOD");
        list.add("JSONSEND");
        String sql = "insert into logs(LOGID,USERNAME,MODULENAME,FUNCTIONNAME" +
                ",SYSTIME,SYSMEMO,OPERATETIME,IP,EXCEPTION,REQUESTURL,TYPE,METHOD,JSONSEND) values(?,?,?,?,?,?,?,?,?,?,?,?,?) ";
        return DbUtility.execSQLWithTrans(sql, list, params);
    }
}
