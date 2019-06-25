package com.tskj.report.scriptlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.sf.jasperreports.engine.JRDefaultScriptlet;

public class EasyStarScript extends JRDefaultScriptlet {
    public static JSONArray datas;

    public static String max(String fieldName) {
        String result = "";
        for (Object object : datas.toArray()) {
            JSONObject jsonObject = (JSONObject) object;
            if (jsonObject.containsKey(fieldName)){
                if (result.compareToIgnoreCase(jsonObject.getString(fieldName)) < 0) {
                    result = jsonObject.getString(fieldName);
                }
            }
        }
        System.out.println(result);
        return result;
    }

    public static String min(String fieldName) {
        String result = "";
        for (Object object : datas.toArray()) {
            JSONObject jsonObject = (JSONObject) object;
            if (jsonObject.containsKey(fieldName)){
                if (result.compareToIgnoreCase(jsonObject.getString(fieldName)) > 0) {
                    result = jsonObject.getString(fieldName);
                }
            }
        }
        return result;
    }
}
