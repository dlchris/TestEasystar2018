package com.tskj.classtable.impl;

import com.tskj.classtable.biz.ClassTableImpl;
import com.tskj.core.system.consts.EasyStarConsts;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件级操作
 * @author LeonSu
 */
public class ClassTableDocImpl extends ClassTableImpl {
    public ClassTableDocImpl(String classId, String tableName, String perFixDes) throws SQLException {
        super(classId, tableName, perFixDes);
        setClassType(Integer.valueOf(EasyStarConsts.DOC));
        setKeyFieldName(EasyStarConsts.KEY_DOC);
        setFields();
    }

    public ClassTableDocImpl(String tableName) throws SQLException {
        super(tableName);
        setClassType(Integer.valueOf(EasyStarConsts.DOC));
        setKeyFieldName(EasyStarConsts.KEY_DOC);
        setFields();
    }
}
