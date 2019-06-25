package com.tskj.classtable.impl;

import com.tskj.classtable.biz.ClassTableImpl;
import com.tskj.core.system.consts.EasyStarConsts;

import java.sql.SQLException;

/**
 * 案卷级的操作
 * @author LeonSu
 */
public class ClassTableRoolImpl extends ClassTableImpl {
    public ClassTableRoolImpl(String classId, String tableName, String perFixDes) throws SQLException {
        super(classId, tableName, perFixDes);
        setClassType(Integer.valueOf(EasyStarConsts.ROOL));
        setKeyFieldName(EasyStarConsts.KEY_ROOL);
        setFields();
    }

    public ClassTableRoolImpl(String tableName) throws SQLException {
        super(tableName);
        setClassType(Integer.valueOf(EasyStarConsts.ROOL));
        setKeyFieldName(EasyStarConsts.KEY_ROOL);
        setFields();
    }
}
