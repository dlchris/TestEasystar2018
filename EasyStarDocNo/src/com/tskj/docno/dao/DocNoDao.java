package com.tskj.docno.dao;

import com.tskj.core.db.DbUtility;
import com.tskj.docno.bean.DocNoFieldInfo;

import java.util.List;
import java.util.Map;

public class DocNoDao {
    public String getPerfixDes(String classId) {
        String sql = "SELECT PERFIXDES FROM CLASSTREE WHERE CLASSID='" + classId + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list == null || list.size() == 0) {
            return "";
        }
        return list.get(0).get("PERFIXDES").toString().trim();
    }

    public int getFieldSize(String tableName, String fieldName) {
        String sql = "select fieldsize from docframe where tablename='" + tableName + "' and fieldname='" + fieldName + "'";
        return Integer.valueOf(DbUtility.execSQL(sql).get(0).get("fieldsize").toString());
    }

    public List<Map<String, Object>> getFieldsSize(String tableName) {
        String sql = "select fieldname,fieldsize,fieldtype from docframe where tablename='" + tableName + "'";
        return DbUtility.execSQL(sql);
    }

    private String getFieldName(String value) {
        int tmpPos = value.indexOf("(");
        String tmpFieldName;
        if (tmpPos >= 0) {
            tmpFieldName = value.substring(0, tmpPos);
        } else {
            tmpFieldName = value;
        }
        tmpPos = tmpFieldName.indexOf("_ID");
        if (tmpPos >= 0) {
            return tmpFieldName.substring(0, tmpPos);
        } else {
            return tmpFieldName;
        }
    }

    public String getDocNoSort(String perFixDes) {
        String[] tmpList = perFixDes.split("\\+");
        if (tmpList.length == 0) {
            return "";
        }
        String fieldName = " case ";
        int i = 0;
        for (String tmpValue : tmpList) {
            if (tmpValue.isEmpty()) {
                continue;
            }
            fieldName += "when FIELDNAME='" + getFieldName(tmpValue) + "' then " + i + " ";
            i++;
        }

        return fieldName.concat(" else 1000 end");
    }

//    public String getDocNoSort(String classId) {
//        String sql = "SELECT PERFIXDES FROM CLASSTREE WHERE CLASSID='" + classId + "'";
//        List<Map<String, Object>> list = DbUtility.execSQL(sql);
//        if (list == null || list.size() == 0) {
//            return "";
//        }
//        String perFixDes = list.get(0).get("PERFIXDES").toString().trim();
//        return getDocNoSortByDes(perFixDes);
//    }
}
