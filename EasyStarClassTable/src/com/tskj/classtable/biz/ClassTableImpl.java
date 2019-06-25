package com.tskj.classtable.biz;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.db.DbUtility;
import com.tskj.core.system.consts.DBConsts;
import com.tskj.core.system.consts.FileType;
import com.tskj.core.system.utility.Tools;
import com.tskj.docframe.dao.DictManager;
import com.tskj.docframe.dao.DocFrameManager;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件级表实体类
 *
 * @author LeonSu
 */
public abstract class ClassTableImpl implements ClassTableBiz {
    private String keyFieldName;
    private String classId;
    private int classType;
    private String tableName;
    private List<Map<String, Object>> fields;
    private List<Map<String, Object>> dictFields = new ArrayList<>();
    private List<Map<String, Object>> isFullFields = new ArrayList<>();
    private String perFixDes;
    private Map<String, Object> mapEx;

    public ClassTableImpl(String tableName) {
        setTableName(tableName);
    }

    public ClassTableImpl(String classId, String tableName, String perFixDes) {
        setPerFixDes(perFixDes);
        setClassId(classId);
        setTableName(tableName);
        setKeyFieldName("");
        setClassType(0);
    }

    public List<Map<String, Object>> getIsFullFields() {
        return isFullFields;
    }

    public String getPerFixDes() {
        return perFixDes;
    }

    protected void setPerFixDes(String value) {
        perFixDes = value;
    }

    protected void setClasstype(int classType) {
        this.classType = classType;
    }

    @Override
    public String getKeyFieldName() {
        return keyFieldName;
    }

    protected void setKeyFieldName(String value) {
        this.keyFieldName = value;
    }

    @Override
    public List<Map<String, Object>> getFields() {
        return fields;
    }

    // TODO 增加 IDSIZE 查字典项长度
    protected void setFields() throws SQLException {
        DocFrameManager docFrameManager = new DocFrameManager();
        fields = docFrameManager.getDocframeWithSort(tableName, perFixDes, "DISSTR, FIELDNAME, FIELDSIZE, FIELDTYPE, DICTTABLE, FIELDSTATE, DOCNO,IDSIZE,ISFILL");

        if (fields != null) {
            for (Map<String, Object> map : fields) {
                //是字典项加入字典项
                if (map.get("DICTTABLE") != null && !map.get("DICTTABLE").toString().trim().isEmpty()) {
                    dictFields.add(map);
                }
                //是必填项加入必填项
                if (map.get("ISFILL") != null && "0".equals(map.get("ISFILL").toString().trim())) {
                    isFullFields.add(map);
                }

            }
        }
    }

    @Override
    public List<Map<String, Object>> getData(Map<String, Object> paramsMap, int limit, int page, String sort, String sortType) {
        Object[] objects = new Object[8];
        objects[0] = tableName;
        objects[1] = keyFieldName;
        objects[2] = limit;
        objects[3] = page;
        objects[4] = " * ";
        objects[5] = sort;
        //objects[6] = null;
        objects[6] = paramsMap.get("power");
        objects[7] = null;
        return DbUtility.execSQL("{ call Paging_SubQuery(?, ?, ?, ?, ?, ?, ?, ?) }", objects);
    }

    @Override
    public List<Map<String, Object>> getDictFields() {
        return this.dictFields;
    }


    @Override
    public boolean isFullValidate(Map<String, Object> value) {
        boolean flag = true;
        for (Map<String, Object> map : isFullFields) {
            if (value.containsKey(map.get("FIELDNAME"))) {
                if ("".equals(Tools.toString(value.get(map.get("FIELDNAME"))))) {
                    flag = false;
                    break;
                }
            } else {
                flag = false;
                break;
            }
        }
        return flag;
    }

    @Override
    public boolean isFullVldUpdate(Map<String, Object> value) {
        boolean flag = true;
        for (Map<String, Object> map : isFullFields) {
            if (value.containsKey(map.get("FIELDNAME"))) {
                if ("".equals(Tools.toString(value.get(map.get("FIELDNAME"))).trim())) {
                    flag = false;
                    //System.err.println("必填项为空");
                    break;
                }
            } else {
                flag = false;
                break;
            }
        }
        return flag;
    }

    @Override
    public List<Map<String, Object>> findFieldNumByDept(String YEARNO, String CLASSID, String TABLENAME) {
        //TODO 目前只支持mysql
        String sqlStr = "";
        if (YEARNO != null && !YEARNO.isEmpty()) {
            sqlStr = "WHERE YEARNO = '" + YEARNO + "'";
        }
        String sql = "SELECT DEPARTMENT,count(DEPARTMENT_ID) AS fieldNum " +
                "FROM '" + TABLENAME + sqlStr + "' GROUP BY DEPARTMENT_ID";
        System.err.println(sql);

        return null;
    }

    /**
     * 根据主键的列表搜索数据
     *
     * @param keyValues     主键列表
     * @param sortFieldName 排序的字段
     * @return
     */
    public List<Map<String, Object>> getList(Object[] keyValues, String sortFieldName) {
        String fieldStr = "";
        for (Map<String, Object> map : fields) {
            fieldStr += String.format("%s as '%s',", map.get("FIELDNAME").toString(), map.get("DISSTR"));
        }
        StringBuilder values = new StringBuilder();
        values.append("'" + keyValues[0].toString().trim() + "'");
        for (int i = 1; i < keyValues.length; i++) {
            values.append(",'" + keyValues[i] + "'");
        }
        String sql = "SELECT " + fieldStr.substring(0, fieldStr.length() - 1) + " FROM " + tableName + " WHERE " + keyFieldName + " in (" + values.toString() + ")";
        sql += " ORDER BY " + sortFieldName;
        return DbUtility.execSQL(sql);
    }

    @Override
    public int getSize(String power) {
        String sqlStr = "";
        if (!"".equals(power)) {
            sqlStr = " WHERE " + power;
        }
        String sql = "SELECT COUNT(*) AS 'count' FROM " + tableName + sqlStr;
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list == null || list.size() == 0) {
            return 0;
        }
        return Integer.valueOf(list.get(0).get("count").toString());
    }

    /**
     * 档案库类型
     *
     * @return 整型，0：文件级，1：盒级，2：案卷级
     */
    @Override
    public int getClassType() {
        return classType;
    }

    protected void setClassType(int value) {
        this.classType = value;
    }

    @Override
    public String getClassId() {
        return classId;
    }

    protected void setClassId(String value) {
        this.classId = value;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    protected void setTableName(String value) {
        this.tableName = value;
    }

    private String getUpdateFields() {
        String tmpStr = "";
        for (Map<String, Object> map : fields) {
            tmpStr += map.get("FIELDNAME").toString() + "=?,";
        }
        for (Map.Entry<String, Object> entry : mapEx.entrySet()) {
            tmpStr += entry.getKey() + "=?, ";
        }
        tmpStr += keyFieldName + "=?";
        return tmpStr;
    }

    private List<String> createParam() {
        List<String> tmpList = new ArrayList<>();
        for (Map<String, Object> map : fields) {
            tmpList.add(map.get("FIELDNAME").toString());
        }
        for (Map.Entry<String, Object> entry : mapEx.entrySet()) {
            tmpList.add(entry.getKey());
        }
        tmpList.add(keyFieldName);
        return tmpList;
    }

    //有两个方法在使用  修改时注意冲突，（新增条目保存）和（条件查询条目都调用了该方法）
    private String getInsertFields() {
        String tmpStr = "";
        for (Map<String, Object> map : fields) {
            tmpStr += map.get("FIELDNAME").toString() + ",";
        }
        if (mapEx != null) {
            for (Map.Entry<String, Object> entry : mapEx.entrySet()) {
                tmpStr += entry.getKey() + ",";
            }
        }

        tmpStr += keyFieldName;
        return tmpStr;
    }

    private String getInsertValues() {
        String tmpStr = "";
        for (int i = 0; i < fields.size(); i++) {
            tmpStr += "?,";
        }
        for (Map.Entry<String, Object> entry : mapEx.entrySet()) {
            tmpStr += "?,";
        }
        tmpStr += "?";
        return tmpStr;
    }

    private void checkKeyValue(Map<String, Object> value) {
        if (!value.containsKey(keyFieldName)) {
            value.put(keyFieldName, Tools.newId());
        } else {
            if (value.get(keyFieldName).toString().isEmpty()) {
                value.put(keyFieldName, Tools.newId());
            }
        }
    }


    @Override
    public JSONObject save(Map<String, Object> value) {

        keyFieldName = keyFieldName.toUpperCase();
        //判断是否需要增加主键
        checkKeyValue(value);
        //增加字典项对应ID
        addDictID(value);
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        String sql;
        String keyValue;

        List<Map<String, Object>> values = new ArrayList<>();
        //TODO  先判断档号是否为空
        //判断档号是否存在 ，除了自己以外是否存在
        if (docIsNoExist(value, keyFieldName)) {
            jsonReturn.put("code", 1);
            jsonReturn.put("errMsg", "档号有重复");
            return jsonReturn;
        }

        values.add(value);
        if (!StringUtils.isEmpty(keyFieldName) && value.containsKey(keyFieldName)) {
            keyValue = value.get(keyFieldName).toString();
            //判断是新增还是修改
            if (exists(keyValue)) {
                //判断必填项是否填写
                //记录存在，更新
                sql = "UPDATE " + tableName + " SET " + getUpdateFields() + " WHERE " + keyFieldName + "='" + keyValue + "'";
                System.out.println(sql);
               /* //修改条目不进行必填项验证
                if (isFullValidate(value)) {
                    //记录存在，更新
                    sql = "UPDATE " + tableName + " SET " + getUpdateFields() + " WHERE " + keyFieldName + "='" + keyValue + "'";
                    System.out.println(sql);
                } else {
                    jsonReturn.put("code", 1);
                    jsonReturn.put("errMsg", "必填项未填写完全");
                    return jsonReturn;
                }*/
            } else {
                if (isFullVldUpdate(value)) {
                    //记录不存在，插入
                    sql = "INSERT INTO " + tableName + " (" + getInsertFields() + ") VALUES (" + getInsertValues() + ")";
                    System.out.println(sql);
                } else {
                    jsonReturn.put("code", 1);
                    jsonReturn.put("errMsg", "必填项未填写完全");
                    return jsonReturn;
                }
            }
            List<String> params = createParam();
            if (DbUtility.execSQLWithTrans(sql, params, values) == 0) {
                jsonReturn.put("code", 0);
            } else {
                jsonReturn.put("code", 1);
                jsonReturn.put("errMsg", "保存失败,添加字段类型或长度存在问题!!!");
            }
        } else {
            jsonReturn.put("code", 2);
            jsonReturn.put("errMsg", "没有主键");
        }
        return jsonReturn;
    }

    //档号是否存在  除了自己以外是否存在
    public boolean docIsNoExist(Map<String, Object> value, String keyFieldName) {

        String ret = "";
        Boolean found = false;
        for (Map.Entry<String, Object> entry : value.entrySet()) {

            if (keyFieldName.equals(entry.getKey())) {
                ret += entry.getKey() + "!='" + entry.getValue().toString() + "' AND ";
            } else if ("DOCNO".equalsIgnoreCase(entry.getKey())) {
                //档号值 為空判断
                String docNoValue = entry.getValue().toString();
                if ("".equals(docNoValue)) {//如果为空直接返回false,为空不需要验证是否重复
                    return false;
                } else {
                    ret += entry.getKey() + "='" + entry.getValue().toString() + "' AND ";
                }
            }
        }
        ret = ret.substring(0, ret.length() - 4);
        //String sql = "SELECT COUNT(*) as 'count' FROM " + tableName + " WHERE " + ret;
        String sql = "SELECT COUNT(*) as count FROM " + tableName + " WHERE " + ret;
        System.err.println(sql);
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list.size() > 0) {
            String count = list.get(0).get("count").toString();
            int a = Integer.parseInt(count);
            found = a > 0;
            //found = ((Integer) list.get(0).get("count")) > 0;
        }
        return found;
    }


    //添加字典项
    public void addDictID(Map<String, Object> fieldMap) {
        //获取档号组成项的字典项对应的DICTID
        DictManager dictManager = new DictManager();
        List<Map<String, Object>> list = dictManager.getDict(tableName);
        //档号组成项的字典项
        mapEx = new HashMap<>();
        for (Map<String, Object> dictField : dictFields) {
            //是字典项
            if (fieldMap.containsKey(dictField.get("FIELDNAME"))) {
                String dictid = "";
                String dictKey = dictField.get("FIELDNAME") + "_ID";
                for (Map<String, Object> map : list) {
                    String fieldname = Tools.toString(fieldMap.get(dictField.get("FIELDNAME"))).trim();
                    if (map.containsValue(fieldname)) {
                        dictid = map.get("DICTID").toString();
                        break;
                    } else {

                    }
                }
                //长度不为空
                if (!"".equals(dictid)) {
                    mapEx.put(dictKey, dictid);
                } else {//为空通过字典项长度生成一个ID 如：0000 (改到档号生成中)
                    mapEx.put(dictKey, "");
                }
            }
        }
        //System.err.println(mapEx);
        fieldMap.putAll(mapEx);
        //System.err.println(fieldMap);
    }


    @Override
    public Boolean exists(String keyValue) {
        if (StringUtils.isEmpty(keyFieldName)) {
            return false;
        }
        Map<String, Object> map = new HashMap<>();
        map.put(keyFieldName, keyValue);
        return exists(map);
    }

    /* @Override
        public JSONObject save(Map<String, Object> value) {
            checkKeyValue(value);
            JSONObject jsonReturn = JSONObject.parseObject("{}");
            String sql;
            String keyValue;
            List<String> params = createParam();
            List<Map<String, Object>> values = new ArrayList<>();
            values.add(value);
            if (!StringUtils.isEmpty(keyFieldName) && value.containsKey(keyFieldName)) {
                keyValue = value.get(keyFieldName).toString();
                if (exists(keyValue)) {
                    if (isFullValidate(value)) {
                        //记录存在，更新
                        sql = "UPDATE " + tableName + " SET " + getUpdateFields() + " WHERE " + keyFieldName + "='" + keyValue + "'";
                        System.out.println(sql);
                    } else {
                        jsonReturn.put("code", 1);
                        jsonReturn.put("errMsg", "必填项未填写完全");
                        return jsonReturn;
                    }
                } else {
                    if (isFullVldUpdate(value)) {
                        //记录不存在，插入
                        sql = "INSERT INTO " + tableName + " (" + getInsertFields() + ") VALUES (" + getInsertValues() + ")";
                        System.out.println(sql);
                    } else {
                        jsonReturn.put("code", 1);
                        jsonReturn.put("errMsg", "必填项未填写完全");
                        return jsonReturn;
                    }
                }
                if (DbUtility.execSQLWithTrans(sql, params, values) == 0) {
                    jsonReturn.put("code", 0);我
                } else {
                    jsonReturn.put("code", 1);
                    jsonReturn.put("errMsg", "保存失败");
                }
            } else {
                jsonReturn.put("code", 2);
                jsonReturn.put("errMsg", "没有主键");
            }
            return jsonReturn;
        }*/
    @Override
    public Boolean exists(Map<String, Object> keyValue) {
        if (StringUtils.isEmpty(keyFieldName)) {
            return false;
        }
        Boolean found = false;
        //String sql = "SELECT COUNT(*) as 'count' FROM " + tableName + " WHERE " + getWhereStr(keyValue);
        String sql = "SELECT COUNT(*) as count FROM " + tableName + " WHERE " + getWhereStr(keyValue);
        //System.err.println(sql);
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list.size() > 0) {
            String count = list.get(0).get("count").toString();
            int a = Integer.parseInt(count);
            found = a > 0;
            //found = ((Integer) list.get(0).get("count")) > 0;
        }
        return found;
    }

    @Override
    public JSONObject delete(String keyValue) {
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        String sql = "DELETE FROM " + tableName + " WHERE " + keyFieldName + "='" + keyValue + "'";
        System.out.println(sql);
        if (DbUtility.execSQLWithTrans(sql) == 0) {
            jsonReturn.put("code", 0);
        } else {
            jsonReturn.put("code", 1);
            jsonReturn.put("errMsg", "删除失败");
        }
        return jsonReturn;
    }

    private String getWhereStr(Map<String, Object> whereValue) {
        String ret = "";
        for (String key : whereValue.keySet()) {
            if (!"$index".equals(key)) {
                ret += key + "='" + whereValue.get(key).toString() + "' AND ";
            }
        }
        return ret.substring(0, ret.length() - 4);
    }

    @Override
    public JSONObject delete(JSONObject whereValue) {
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        String sql = "DELETE FROM " + tableName + " WHERE " + getWhereStr(whereValue);
        System.out.println(sql);
        if (DbUtility.execSQLWithTrans(sql) == 0) {
            jsonReturn.put("code", 0);
        } else {
            jsonReturn.put("code", 1);
            jsonReturn.put("errMsg", "删除失败");
        }
        return jsonReturn;
    }

    @Override
    public int delete(List<String> keyValues) {
        String keys = keyValues.toString().replace(", ", "', '").replace("[", "'").replace("]", "'");
        String sql = "DELETE FROM " + tableName + " WHERE " + keyFieldName + " in(" + keys + ")";
        System.out.println(sql);
        int ret = DbUtility.execSQLWithTrans(sql);
//        int ret = 0;
        return ret;
    }

    @Override
    public JSONObject delete(JSONArray values) {
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        JSONArray jsonResults = JSONArray.parseArray("[]");
        JSONObject jsonResult = JSONObject.parseObject("{}");
        JSONObject map;
        boolean flag = false;
        String getKey = "";
        List<String> keyList = null;
        for (Object obj : values) {
            map = (JSONObject) obj;
            for (String str : map.keySet()) {
                if (str.toUpperCase().equals(keyFieldName)) {
                    getKey = str;
                    flag = true;
                    break;
                }
            }
            if (flag) {
//                jsonResult = delete(map.get(keyFieldName).toString());
                jsonResult = delete(map.get(getKey).toString());
            } else {
                jsonResult.put("code", 1);
                jsonResult.put("errMsg", "缺少档案ID");
            }
            jsonResults.add(jsonResult);
        }
        jsonReturn.put("code", 0);
        jsonReturn.put("data", jsonResults);
        return jsonReturn;
    }

    @Override
    public JSONObject singleDel(JSONObject value) {
        String keyValue = "";
        for (String str : value.keySet()) {
            if (str.toUpperCase().equals(keyFieldName)) {
                keyValue = value.getString(str);          //拿出主键放进集合中
                break;
            }
        }
        return delete(keyValue);
    }

    @Override
    public JSONObject mutilDel(JSONArray values) {
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        JSONObject map;
        List<String> keyList = new ArrayList<>();
        for (Object obj : values) {
            map = (JSONObject) obj;
            for (String str : map.keySet()) {
                if (str.toUpperCase().equals(keyFieldName)) {
                    keyList.add(map.getString(str));          //拿出主键放进集合中
                    break;
                }
            }
        }
        int ret = delete(keyList);         //批量删除
        if (ret == 0) {
            jsonReturn.put("code", 0);
            jsonReturn.put("data", "删除成功！");
        } else {
            jsonReturn.put("code", 1);
            jsonReturn.put("data", "删除失败！");
        }
        return jsonReturn;
    }

    private List<Map<String, Object>> data;

    public List<Map<String, Object>> getData() {
        return data;
    }

    @Override
    public Boolean search(String whereStr, String sortFields, String power, String classType) {
        //( SELECT COUNT( * ) FROM files f WHERE f.DOCID = document88050c556e4e4988.DOCID ) AS filesNum
        String sqlStr = "";
        switch (classType.toUpperCase()) {
            case "DOCID":
                sqlStr = ",( SELECT COUNT( * ) FROM files f WHERE f.DOCID = " + tableName + ".DOCID ) AS filesNum";
                break;
            default:
                break;
        }
        String sql = "";
        switch (DbUtility.getDBType()) {
            case DBConsts.DB_MYSQL:
                sql = "select " + getInsertFields() + sqlStr + " from " + tableName + " where ifnull(isdel,'')= '' ";
                break;
            case DBConsts.DB_SQLSERVER:
                sql = "select " + getInsertFields() + sqlStr + " from " + tableName + " where isnull(isdel,'')= '' ";
                break;
            case DBConsts.DB_ORACLE:
                break;
            default:

        }
        if (!whereStr.isEmpty()) {
            sql += "and (" + whereStr + ") ";
        }
        if (!power.isEmpty()) {
            sql += "and " + power + " ";
        }
        if (sortFields != null && !sortFields.isEmpty()) {
            sql += "order by " + sortFields;
        }
        //System.err.println(sql);
        data = DbUtility.execSQL(sql);
        return true;
    }

    @Override
    public JSONObject export(FileType fileType, String fileName, JSONArray datas) {
        JSONObject json = JSONObject.parseObject("{}");
        json.put("code", 0);
        return json;
    }

    private String getFieldNameByDisstr(String disstr) throws Exception {
        if (null == disstr || disstr.isEmpty()) {
            throw new Exception("未找到字段");
        }
        for (Map<String, Object> map : fields) {
            if (disstr.equals(map.get("DISSTR").toString().trim())) {
                return map.get("FIELDNAME").toString().trim();
            }
        }
        throw new Exception("未找到字段");
    }

    public static void main(String[] args) {
        /*Map<String, Object> value = new HashMap<>();
        value.put("DOCID", "123");
        value.put("ABV", "321");

        Map<String, Object> v = new CaseInsensitiveMap<>();

        v.putAll(value);
        System.err.println(v);
        System.err.println(v.containsKey("docId"));*/

        List<String> list = new ArrayList<>();
        list.add("aaa");
        list.add("aaa");
        list.add("aaa");
        list.add("aaa");
        list.add("aaa");
        System.out.println(list.toString());

    }
}
