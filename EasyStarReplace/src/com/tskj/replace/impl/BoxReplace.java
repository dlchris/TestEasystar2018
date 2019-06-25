package com.tskj.replace.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.replace.biz.EasyStarReplace;

public class BoxReplace extends EasyStarReplace {
    public BoxReplace(String classId, String tableName, JSONObject jsonObject, DocNoEngine docNoEngine) throws Exception {
        super(classId, tableName, jsonObject,docNoEngine);
        setKeyFieldName("boxid");
    }

    public static void main(String[] args) {
        JSONObject json = JSONObject.parseObject("{}");
        json.put("type", 3);
        json.put("sourceFieldName", "docname");
        json.put("targetFieldName", "caseno");
        json.put("newValue", "0");
        json.put("oldValue", "a");
        json.put("addValue", -1);
        JSONArray list = JSONArray.parseArray("[]");
        JSONObject obj = JSONObject.parseObject("{}");
        obj.put("id", "1");
        list.add(obj);
        obj = JSONObject.parseObject("{}");
        obj.put("id", "2");
        list.add(obj);
        json.put("list", list);
        try {
            //BoxReplace boxReplace = new BoxReplace("7114C8BDC95041119DFE7226F525CC94", "BOXINFO88050C556E4E4988", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
