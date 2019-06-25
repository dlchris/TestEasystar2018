package com.tskj.replace.impl;

import com.alibaba.fastjson.JSONObject;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.replace.biz.EasyStarReplace;

public class DocReplace extends EasyStarReplace {
    public DocReplace(String classId, String tableName, JSONObject jsonObject, DocNoEngine docNoEngine) throws Exception {
        super(classId, tableName, jsonObject,docNoEngine);
        setKeyFieldName("docid");
    }
}
