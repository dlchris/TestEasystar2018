package com.tskj.classtable.statistics.impl;

import com.alibaba.fastjson.JSONArray;
import com.tskj.classtable.statistics.biz.ClassTableStatisticsBiz;
import com.tskj.classtable.statistics.consts.StatisticsType;
import com.tskj.core.system.utility.Tools;
import com.tskj.docframe.dao.DocFrameManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 按照年度统计各保管期限的个数
 *
 * @author LeonSu
 */
public class ClassTableStatistics extends ClassTableStatisticsBiz {
    public ClassTableStatistics(String tableName) throws SQLException {
        super(tableName);
        DocFrameManager docFrameManager = new DocFrameManager();
        fieldNameList = docFrameManager.getDocframe(tableName, "FIELDNAME,DISSTR");
    }

    // 查询字段fileName
    String keyName;
    List<Map<String, Object>> fieldNameList = new ArrayList<>();

    /**
     * 2019-06-18
     * 按照年度统计各保管期限的个数
     *
     * @param whereStr 查询条件
     * @return 成功：true，结果保存在data属性中，失败：false
     * @throws Exception
     */
    public Boolean saveDate(String whereStr) throws Exception {
        JSONArray jsonArray = JSONArray.parseArray("[]");
        DocFrameManager docFrameManager = new DocFrameManager();
        List<Map<String, Object>> fields = docFrameManager.getDocframe(this.getTableName(), "FIELDNAME", "保管期限");
        jsonArray.add(fields.get(0).get("FIELDNAME").toString());
        String yearNo = "";
        fields = docFrameManager.getDocframe(this.getTableName(), "FIELDNAME", "年度");
        yearNo = fields.get(0).get("FIELDNAME").toString();
        return statistics(StatisticsType.COUNT, jsonArray, whereStr, yearNo);
    }


    public Boolean findYearDate(String whereStr) throws Exception {
        JSONArray jsonArray = JSONArray.parseArray("[]");
        DocFrameManager docFrameManager = new DocFrameManager();
        List<Map<String, Object>> fields = docFrameManager.getDocframe(this.getTableName(), "FIELDNAME", "保管期限");
        if (fields != null && !fields.isEmpty()) {
            jsonArray.add(fields.get(0).get("FIELDNAME").toString());
            //保存查询字段名称
            this.keyName = fields.get(0).get("FIELDNAME").toString();
        } else {
            return false;
        }

        String yearNo = "";
        //-1 查全部,不是-1查所有
        if (!"".equals(whereStr)) {
            fields = docFrameManager.getDocframe(this.getTableName(), "FIELDNAME", "年度");
            if (fields != null && !fields.isEmpty()) {
                yearNo = fields.get(0).get("FIELDNAME").toString();
                whereStr = yearNo + "=" + whereStr;
            } else {
                return false;
            }

        }
        return statistics(StatisticsType.COUNT, jsonArray, whereStr, yearNo);
    }

    public Boolean findDeptCount(String whereStr) throws Exception {

        JSONArray jsonArray = JSONArray.parseArray("[]");
        DocFrameManager docFrameManager = new DocFrameManager();
        List<Map<String, Object>> fields = docFrameManager.getDocframe(this.getTableName(), "FIELDNAME", "归档机构");
        if (fields != null && !fields.isEmpty()) {
            jsonArray.add(fields.get(0).get("FIELDNAME").toString());
            this.keyName = fields.get(0).get("FIELDNAME").toString();
        } else {
            return false;
        }
        String yearNo = "";
        //-1 查全部,不是-1查所有
        if (!"".equals(whereStr)) {
            fields = docFrameManager.getDocframe(this.getTableName(), "FIELDNAME", "年度");
            if (fields != null && !fields.isEmpty()) {
                String yearName = fields.get(0).get("FIELDNAME").toString();
                whereStr = yearName + "=" + whereStr;
            } else {
                return false;
            }
        }
        System.err.println(whereStr);
        return statistics(StatisticsType.COUNT, jsonArray, whereStr, yearNo);


    }

    /**
     * 统计档案个数
     *
     * @return 成功：true，结果保存在data属性中，失败：false
     * @throws Exception
     */
    public Boolean count() throws Exception {
        JSONArray jsonArray = JSONArray.parseArray("[]");
        jsonArray.add("*");
        return statistics(StatisticsType.COUNT, jsonArray, "", "");
    }

    /**
     * 统计各年度下的档案个数
     *
     * @return
     * @throws Exception
     */
    public Boolean yearNo() throws Exception {
        String whereStr = "";
        JSONArray jsonArray = JSONArray.parseArray("[]");
        DocFrameManager docFrameManager = new DocFrameManager();
        String yearNo = "";
        List<Map<String, Object>> fields = docFrameManager.getDocframe(this.getTableName(), "FIELDNAME", "年度");
        yearNo = fields.get(0).get("FIELDNAME").toString();
        jsonArray.add(yearNo);
        return statistics(StatisticsType.COUNT, jsonArray, whereStr, yearNo);
    }

    //查询各机构档案件数

    /**
     * @param saveDate   保管期限
     * @param department 哪个部门
     * @param yearStar   哪年开始
     * @param yearEnd    哪年结束
     * @return
     * @Author JRX
     * @Description:
     * @create 2019/6/25 14:43
     **/
    public boolean FindDeptSaveDateFileNum(String saveDate, String department, String yearStar, String yearEnd) throws SQLException {
        JSONArray jsonArray = JSONArray.parseArray("[]");

        DocFrameManager docFrameManager = new DocFrameManager();
        List<Map<String, Object>> fields = docFrameManager.getDocframe(this.getTableName(), "FIELDNAME", "归档机构");
        fields = docFrameManager.getDocframe(this.getTableName(), "FIELDNAME", "保管期限");
        if (fields != null && !fields.isEmpty()) {
            jsonArray.add(fields.get(0).get("FIELDNAME").toString());
            this.keyName = fields.get(0).get("FIELDNAME").toString();
        } else {
            return false;
        }
        String yearNo = "";
        //-1 查全部,不是-1查所有
       /* if (!"".equals(whereStr)) {
            fields = docFrameManager.getDocframe(this.getTableName(), "FIELDNAME", "年度");
            if (fields != null && !fields.isEmpty()) {
                String yearName = fields.get(0).get("FIELDNAME").toString();
                whereStr = yearName + "=" + whereStr;
            } else {
                return false;
            }
        }
        System.err.println(whereStr);*/
        //return statistics(StatisticsType.COUNT, jsonArray, whereStr, yearNo);
        return true;
    }


    public static void main(String[] args) {
        ClassTableStatistics cts = null;
        try {
            cts = new ClassTableStatistics("DOCUMENT88050C556E4E4988");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /*try {
         *//*  if (cts.yearNo()) {
                System.err.println(cts.getData());
            }*//*
         *//*if (cts.saveDate("YEARNO = '2018' ")) {
                System.err.println(cts.getData());
            }*//*
            if (cts.findDeptCount("YEARNO = '2018' ")) {
                System.err.println(cts.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        /*JSONArray jsonArray = JSONArray.parseArray("[]");
        jsonArray.add("YEARNO");
        String whereStr = "FIELD0B546940487B4AAA ='永久' AND DEPARTMENT = '联络处' ";
        try {
            cts.statistics(StatisticsType.COUNT, jsonArray, whereStr, "YEARNO");
            System.err.println(cts.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        DocFrameManager docFrameManager = new DocFrameManager();
        List<Map<String, Object>> fieldNameList = null;
        try {
            fieldNameList = docFrameManager.getDocframe("DOCUMENT88050C556E4E4988", "FIELDNAME,DISSTR");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.err.println(fieldNameList);
        System.err.println(fieldNameList.get(0));

    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    //,String keyName
    public List<Map<String, Object>> formatListMap(List<Map<String, Object>> listParam) {
        for (Map<String, Object> o : listParam) {
            o.put("name", o.remove(keyName));
            o.put("value", o.remove("COUNT1"));
        }
        return listParam;
    }
}
