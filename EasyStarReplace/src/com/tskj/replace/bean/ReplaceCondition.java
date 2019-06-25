package com.tskj.replace.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.db.DbUtility;
import com.tskj.core.system.utility.Tools;
import com.tskj.docno.bean.DocNoFieldInfo;
import com.tskj.docno.impl.DocNoEngine;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 成批替换的内容bean
 *
 * @author LeonSu
 */
public class ReplaceCondition {
    private ReplaceType replaceType = ReplaceType._NONE;
    private String sourceFieldName = "";
    private String targetFieldName = "";
    private String oldValue = "";
    private String newValue = "";
    private int addValue = 0;
    private JSONArray dataLists = null;
    private boolean isDocNoItem;//是否是档号组成项
    private boolean isDictField;//是否是字典项
    private String dictFieldName;//字典字段名

    public String getDictFieldName() {
        return dictFieldName;
    }

    public boolean getIsDictField() {
        return isDictField;
    }

    public boolean getIsDocNoItem() {
        return isDocNoItem;
    }

    public JSONArray getDataLists() {
        return dataLists;
    }

    public String getOldValue() {
        return oldValue.trim();
    }

    public String getNewValue() {
        return newValue.trim();
    }

    public ReplaceType getReplaceType() {
        return replaceType;
    }

    public String getSourceFieldName() {
        return sourceFieldName;
    }

    public String getTargetFieldName() {
        return targetFieldName;
    }

    public int getAddValue() {
        return addValue;
    }

    public ReplaceCondition(JSONObject jsonObject, DocNoEngine docNoEngine) throws Exception {
        if (jsonObject.containsKey("type")) {
            int index = jsonObject.getIntValue("type") + 1;
            if (index < ReplaceType.values().length) {
                replaceType = ReplaceType.values()[index];
            }
        }
        if (jsonObject.containsKey("list")) {
            dataLists = jsonObject.getJSONArray("list");
        }
        boolean b = true;
        switch (replaceType) {
            case PREFIX:
            case SUFFIX:
            case ALL:
                if (jsonObject.containsKey("sourceFieldName")) {
                    sourceFieldName = jsonObject.getString("sourceFieldName");
                }
                if (jsonObject.containsKey("newValue")) {
                    newValue = jsonObject.getString("newValue");
                }
                break;
            case PARTIAL:
                if (jsonObject.containsKey("sourceFieldName")) {
                    sourceFieldName = jsonObject.getString("sourceFieldName");
                }
                if (jsonObject.containsKey("newValue")) {
                    newValue = jsonObject.getString("newValue");
                }
                if (jsonObject.containsKey("oldValue")) {
                    oldValue = jsonObject.getString("oldValue");
                }
                break;
            case FIELDNAME:
                if (jsonObject.containsKey("sourceFieldName")) {
                    sourceFieldName = jsonObject.getString("sourceFieldName");
                }
                if (jsonObject.containsKey("targetFieldName")) {
                    targetFieldName = jsonObject.getString("targetFieldName");
                }
                break;
            case ADD:
                if (jsonObject.containsKey("sourceFieldName")) {
                    sourceFieldName = jsonObject.getString("sourceFieldName");
                }
                if (jsonObject.containsKey("addValue")) {
                    addValue = jsonObject.getIntValue("addValue");
                }
                break;
            case _NONE:
            default:
                b = false;
                break;
        }
        if (!b) {
            throw new Exception("替换类型不正确");
        }
        //判断是否是档号组成项
        isDocNoItem = docNoEngine.indexOf(sourceFieldName);
        Vector<DocNoFieldInfo> docNoRule = docNoEngine.getDocNoRule();
        isDictField = false;
        //判断是否是字典项
        for (DocNoFieldInfo info : docNoRule) {
            //字段是字典项并且是字典项
             if(info.getFieldName().equalsIgnoreCase(sourceFieldName)&&info.getIsDictField()){
                 isDictField = true;
                 dictFieldName = info.getDictFieldName();
                 break;
             }
        }
    }
}
