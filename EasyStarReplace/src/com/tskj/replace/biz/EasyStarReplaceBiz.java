package com.tskj.replace.biz;

import com.alibaba.fastjson.JSONObject;

public interface EasyStarReplaceBiz {
    String getTableName();
    String getKeyFieldName();
    String getClassId();
    JSONObject replace() throws Exception;
}
