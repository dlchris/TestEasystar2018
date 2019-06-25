package com.tskj.classtable.statistics.biz;

import com.alibaba.fastjson.JSONArray;
import com.tskj.classtable.statistics.consts.StatisticsType;
import com.tskj.core.db.DbUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author LeonSu
 */
public abstract class ClassTableStatisticsBiz {

    private String tableName;

    private List<Map<String, Object>> data;

    public ClassTableStatisticsBiz(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 分组统计
     *
     * @param type        统计类型，SUM、COUNT、MAX、MIN、AVG
     * @param fieldArray  统计的字段数组
     * @param whereStr    查询的条件
     * @param groupFields 分组，排序字段
     * @return 成功：true，结果保存在data属性中，失败：false
     * @throws Exception
     */
    public Boolean statistics(StatisticsType type, JSONArray fieldArray, String whereStr, String groupFields) throws Exception {
        String fieldGroup = "";
        if (groupFields != null) {
            fieldGroup = groupFields;
        }
        String fieldName = "";
        String fields = "";
        String tmpStr;
        String sql;
        switch (type) {
            case COUNT:
            case SUM:
            case MAX:
            case MIN:
            case AVG:
                tmpStr = type.toString();
                break;
            default:
                throw new Exception("统计类型不正确");
        }
        int i = 1;
        for (Object obj : fieldArray) {
            if (!"*".equals(obj.toString().trim())) {
                fields += String.format("%1$s(%2$s) as '%1$s%3$d', %2$s,", tmpStr, obj.toString().trim(), i);
                if (!fieldGroup.isEmpty()) {
                    if (!fieldGroup.contains(obj.toString().trim())) {
                        fieldGroup += "," + obj.toString().trim();
                    }
                } else {
                    fieldGroup = obj.toString().trim();
                }
            } else {
                fields += String.format("%1$s(%2$s) as '%1$s%3$d',", tmpStr, obj.toString().trim(), i);
            }
            i++;
        }
        fields = fields.substring(0, fields.length() - 1);
        sql = String.format("select %s from %s ", fields, tableName);
        if (!whereStr.isEmpty()) {
            sql += "where " + whereStr + " ";
        }
        if (!fieldGroup.isEmpty()) {
            sql += "group by " + fieldGroup + " ";
            sql += "order by " + fieldGroup;
        }
        System.err.println(sql);
        data = DbUtility.execSQL(sql);
        if (data == null) {
            return false;
        }
        return true;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public String getTableName() {
        return tableName;
    }
}
