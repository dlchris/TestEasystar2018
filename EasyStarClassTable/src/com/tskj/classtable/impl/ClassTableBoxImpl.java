package com.tskj.classtable.impl;

import com.tskj.classtable.biz.ClassTableImpl;
import com.tskj.core.system.consts.EasyStarConsts;

import java.sql.SQLException;

/**
 * 盒级操作
 * @author LeonSu
 */
public class ClassTableBoxImpl extends ClassTableImpl {
    public ClassTableBoxImpl(String classId, String tableName, String perFixDes) throws SQLException {
        super(classId, tableName, perFixDes);
        setClassType(Integer.valueOf(EasyStarConsts.BOX));
        setKeyFieldName(EasyStarConsts.KEY_BOX);
        setFields();
    }

    public ClassTableBoxImpl(String tableName) throws SQLException {
        super(tableName);
        setClassType(Integer.valueOf(EasyStarConsts.BOX));
        setKeyFieldName(EasyStarConsts.KEY_BOX);
        setFields();
    }
}
