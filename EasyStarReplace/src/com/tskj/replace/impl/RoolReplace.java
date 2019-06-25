package com.tskj.replace.impl;

import com.alibaba.fastjson.JSONObject;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.replace.biz.EasyStarReplace;

public class RoolReplace extends EasyStarReplace {
    public RoolReplace(String classId, String tableName, JSONObject jsonObject, DocNoEngine docNoEngine) throws Exception {
        super(classId, tableName, jsonObject,docNoEngine);
        setKeyFieldName("roolid");
    }
}
