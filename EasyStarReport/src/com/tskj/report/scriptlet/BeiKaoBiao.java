package com.tskj.report.scriptlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.sf.jasperreports.engine.JRAbstractScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

public class BeiKaoBiao extends JRAbstractScriptlet{
    public static JSONArray datas;
    @Override
    public void beforeReportInit() throws JRScriptletException {
        JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(this.fieldsMap));
        System.out.println(json.toJSONString());
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    }

    @Override
    public void afterReportInit() throws JRScriptletException {

    }

    @Override
    public void beforePageInit() throws JRScriptletException {

    }

    @Override
    public void afterPageInit() throws JRScriptletException {

    }

    @Override
    public void beforeColumnInit() throws JRScriptletException {

    }

    @Override
    public void afterColumnInit() throws JRScriptletException {

    }

    @Override
    public void beforeGroupInit(String s) throws JRScriptletException {

    }

    @Override
    public void afterGroupInit(String s) throws JRScriptletException {

    }

    @Override
    public void beforeDetailEval() throws JRScriptletException {

    }

    @Override
    public void afterDetailEval() throws JRScriptletException {

    }
}
