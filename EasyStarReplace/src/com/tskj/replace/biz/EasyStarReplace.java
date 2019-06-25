package com.tskj.replace.biz;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.core.db.DbConnection;
import com.tskj.core.db.DbUtility;
import com.tskj.core.system.utility.Tools;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.replace.bean.ReplaceCondition;

import java.sql.Connection;
import java.util.*;

/**
 * @author LeonSu
 */
public abstract class EasyStarReplace implements EasyStarReplaceBiz {

    private String classId;
    private String tableName;
    private String keyFieldName;
    private DocNoEngine docNoEngine;
    private List<Map<String, Object>> DictFieldIdList = new ArrayList<>();

    //private Object updataValue;
    private Map<String, Object> docNoMap = new HashMap<>();

    private ReplaceCondition replaceCondition;

    public EasyStarReplace(String classId, String tableName, JSONObject jsonReplaceCondition, DocNoEngine docNoEngine) throws Exception {
        setClassId(classId);
        setTableName(tableName);
        setDocNoEngine(docNoEngine);
        this.replaceCondition = new ReplaceCondition(jsonReplaceCondition, docNoEngine);
    }

    public ReplaceCondition getReplaceCondition() {
        return replaceCondition;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    protected void setKeyFieldName(String value) {
        keyFieldName = value;
    }

    protected void setClassId(String value) {
        classId = value;
    }

    public DocNoEngine getDocNoEngine() {
        return docNoEngine;
    }

    public void setDocNoEngine(DocNoEngine docNoEngine) {
        this.docNoEngine = docNoEngine;
    }


    @Override
    public String getClassId() {
        return classId;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String getKeyFieldName() {
        return keyFieldName;
    }

    @Override
    public JSONObject replace() throws Exception {
        String sql = "";
        boolean b = true;
        //判断是否是档号组成项
        String sqlStr = "";
        /**
         * @create 2019/3/1 10:35
         * 判断
         * 1.是档号组成项并且是字典项查对应字典Id
         * 2.档号只是档号组成项不是字典项
         * 3.都不是
         **/
        if (replaceCondition.getIsDocNoItem() && replaceCondition.getIsDictField()) {
            sqlStr = " docNo = ? , " + replaceCondition.getDictFieldName() + " = ? , ";
        } else if (replaceCondition.getIsDocNoItem()) {
            sqlStr = " docNo = ? , ";
        }

        sql = "UPDATE " + tableName + " SET " + sqlStr + replaceCondition.getSourceFieldName() + "= ? WHERE " + keyFieldName + "=?";

        JSONObject retJson = JSONObject.parseObject("{}");
        int ret;
        //用作临时表ID
        String userId = Tools.newId();

        if (replaceCondition.getDataLists() != null && !replaceCondition.getDataLists().isEmpty()) {
            JSONArray array = replaceCondition.getDataLists();
            String docId;
            Connection conn = DbConnection.getConnection();
            List<String> params = new ArrayList<>();
            List<Map<String, Object>> values = new ArrayList<>();
            String flag;
            if (replaceCondition.getIsDocNoItem() && replaceCondition.getIsDictField()) {
                flag = "dic";//是档号组成项字典项
                params.add("docNo");
                //获取对应的字典项所有ID集合
                setDictFieldIdList();
                params.add(replaceCondition.getDictFieldName());
            } else if (replaceCondition.getIsDocNoItem()) {
                flag = "doc";//是档号组成项非字典项
                params.add("docNo");
            } else {
                flag = "none";//不是字典项
            }
            params.add(replaceCondition.getSourceFieldName());
            params.add(keyFieldName);

            //检查来源数据中有没有重复的档号
            HashSet<String> DocNoSet = new HashSet<>();
            try {
                conn.setAutoCommit(false);
                for (int i = 0; i < array.size(); i++) {
                    docId = array.getString(i);
                    //字段集合
                    Map<String, Object> map = new HashMap<>();
                    //新档号
                    String docNo = "";
                    //生成新字段
                    Object updataValue = getNewSourceValue(conn, tableName, docId);
                    if (updataValue == null) {
                        retJson.put("code", 1);
                        retJson.put("errMsg", "替换类型不符合字段类型");
                        return retJson;
                    }
                    //判断是否需要修改档号
                    JSONObject json = new JSONObject(docNoMap);
                    if ("dic".equals(flag)) {
                        //获取对应的字典字段
                        //String DICTID = DictFieldId(conn, updataValue.toString());
                        String DICTID = getDictFieldId(updataValue.toString());
                        if (DICTID == null) {// 字典字段ID如果没有 这种情况应该不会发生因为前台传过来的替换字段不可能数据库找不到除非改数据包
                            DICTID = "";
                        }
                        json.put(replaceCondition.getDictFieldName(), DICTID);
                        //默认自动补齐
                        docNo = docNoEngine.getNewDocNoByJson(json, true);
                        //不重复才能继续添加
                        if (DocNoSet.add(docNo)) {
                            map.put("docNo", docNo);
                            //档号存入临时表
                            docNoTemp(conn, String.valueOf(i), docNo, userId);
                        } else {//重复直接停止循环
                            retJson.put("code", 1);
                            System.out.println("档号重复为:"+docNo);
                            retJson.put("errMsg", "替换字典项时条目中存在档号重复");
                            return retJson;
                        }

                        map.put(replaceCondition.getDictFieldName(), DICTID);

                    } else if ("doc".equals(flag)) {
                        docNo = docNoEngine.getNewDocNoByJson(json, true);
                        map.put("docNo", docNo);
                        //不重复才能继续添加
                        if (DocNoSet.add(docNo)) {
                            map.put("docNo", docNo);
                            docNoTemp(conn, String.valueOf(i), docNo, userId);
                        } else {//重复直接停止循环
                            System.out.println("档号重复为:"+docNo);
                            retJson.put("code", 1);
                            retJson.put("errMsg", "替换档号组成非字典项时条目中存在档号重复");
                            return retJson;
                        }
                    } else {

                    }
                    map.put(replaceCondition.getSourceFieldName(), updataValue);
                    map.put(keyFieldName, docId);
                    //最终替换后要修改的字段内容
                    values.add(map);
                }

                System.err.println(values);

                if (!"none".equals(flag)) {
                    //判断档号是否有重复
                    List<Map<String, Object>> list = docNoRepeat(conn, userId, tableName);
                    //删除临时表数据
                    int i = delDocNoTemp(conn, userId);
                    if (list != null && !list.isEmpty()) {//不为空有重复不能替换
                        retJson.put("code", 1);
                        retJson.put("errMsg", "修改后档号在表中有重复");
                        return retJson;
                    }
                }
                ret = DbUtility.execSQLWithTrans(conn, sql, params, values);
                if (ret == 1) {
                    conn.rollback();
                    retJson.put("code", 1);
                    retJson.put("errMsg", "替换出错");
                    return retJson;

                }
                conn.commit();
                retJson.put("code", 0);
                retJson.put("errMsg", "替换成功");

            } finally {
                conn.setAutoCommit(true);
                DbConnection.close(conn);
            }
        } else {//不传修改的档号ID 不允许修改
            retJson.put("code", 1);
            retJson.put("errMsg", "没有传修改数据ID集合");
        }
        return retJson;
    }

    //获取对应的档号字典项ID
    public String getDictFieldId(String updataValue) {
        for (Map<String, Object> map : DictFieldIdList) {
            //if(map.containsValue(updataValue)){
            if (updataValue.equals(map.get("DVALUE").toString())) {

                return map.get("DICTID").toString();
            }
        }
        return null;
    }

    //获取对应字典项所有字段值集合
    public void setDictFieldIdList() {
        String sql = "SELECT * FROM (SELECT DICTID,DVALUE,FIELDNAME FROM SYSDICT UNION ALL " +
                "SELECT DICTID,DVALUE,FIELDNAME FROM USERDICT WHERE TABLENAME='" + tableName + "') A  " +
                "WHERE FIELDNAME = '" + replaceCondition.getSourceFieldName() + "' ORDER BY FIELDNAME,DICTID";
        this.DictFieldIdList = DbUtility.execSQL(sql);
    }

    /**
     * @return 过时
     * @Author JRX
     * @Description: 获取字典项的ID   ,如果修改的是字典项，只能是全部替换时其实可以直接获取
     * @create 2019/3/1 10:50
     **/
    //String tableName, String dictFieldName, String DValue
    /*public String DictFieldId(Connection conn, String updataValue) {
        String sql = "SELECT * FROM (SELECT DICTID,DVALUE,FIELDNAME FROM SYSDICT UNION ALL " +
                "SELECT DICTID,DVALUE,FIELDNAME FROM USERDICT WHERE TABLENAME='" + tableName + "') A  " +
                "WHERE DVALUE = '" + updataValue + "' AND FIELDNAME = '" + replaceCondition.getSourceFieldName() + "' ORDER BY FIELDNAME,DICTID";
        //System.err.println(sql);
        List<Map<String, Object>> list = DbUtility.execSQL(conn, sql, null);
        if (list != null && !list.isEmpty()) {
            return list.get(0).get("DICTID").toString();
        }
        return null;
    }*/

    //档号存入临时表
    public int docNoTemp(Connection conn, String IMPORTID, String DOCNO, String USERID) {
        String SQL = "INSERT INTO IMPORTTEMP (IMPORTID,DOCNO,USERID) VALUES ('" + IMPORTID + "','" + DOCNO + "','" + USERID + "'); ";
        return DbUtility.execSQLWithTrans(conn, SQL);
    }

    //删除本次存入临时表的档号
    public int delDocNoTemp(Connection conn, String USERID) {
        String sql = "DELETE FROM IMPORTTEMP WHERE USERID = '" + USERID + "' ";
        return DbUtility.execSQLWithTrans(conn, sql);
    }

    //档号是否有重复
    public List<Map<String, Object>> docNoRepeat(Connection conn, String USERID, String tableName) {
        // 查询出对应导入条目是否存在重复档号集合
        String sql = "SELECT * FROM IMPORTTEMP WHERE USERID='" + USERID + "' AND EXISTS(SELECT A.DOCNO FROM " + tableName + "  A WHERE A.DOCNO=IMPORTTEMP.DOCNO)";
        //System.err.println(sql);
        return DbUtility.execSQL(conn, sql, null);
    }


    /**
     * //* @param docNoEngine 档号引擎用来生成newDocNo
     *
     * @param tableName 表名
     * @param docId     OldDocNo 用来查询原数据集合
     * @return
     * @Author JRX
     * @Description: 生成新的修改字段
     * 两种方法修改档号 1.修改字段是同时修改（此处用第一种方法）
     * 2.修改完字段在循环list出来再更改档号   (这种方法有一个弊端如果第一个update 字段超过字段长度或其他原因造成修改失败等于并没有update成功这时再继续修改档号
     * 有可能档号修改成功字段并没有修改，如果解决比较麻烦还要记录修改集合中那条是不需要update的)
     * @create 2019/2/28 11:23 getNewDocNo
     **/
    protected Object getNewSourceValue(Connection conn, String tableName, String docId) {
        // docNo 替换成keyFieldName
        String sql = "select * from " + tableName + " where " + keyFieldName + " ='" + docId + "'";
        //System.err.println(sql);
        List<Map<String, Object>> list = DbUtility.execSQL(conn, sql, null);
        Object updataValue = "";
        if (list != null && !list.isEmpty()) {
            docNoMap.clear();
            docNoMap = list.get(0);
            //获取原先的字段
            String sourceValue = toString(docNoMap.get(replaceCondition.getSourceFieldName())).trim();
            boolean b = true;
            try {
                switch (replaceCondition.getReplaceType()) {
                    case PREFIX://前缀
                        updataValue = replaceCondition.getNewValue() + sourceValue;
                        System.err.println("增加前缀后:" + updataValue);
                        break;
                    case SUFFIX://后缀
                        updataValue = sourceValue + replaceCondition.getNewValue();
                        System.err.println("增加后缀后:" + updataValue);
                        break;
                    case ALL://替换所有
                        updataValue = replaceCondition.getNewValue();
                        System.err.println("替换所有后:" + updataValue);
                        break;
                    case PARTIAL://部分替换
                        updataValue = sourceValue.replace(replaceCondition.getOldValue(), replaceCondition.getNewValue());
                        System.err.println("部分替换后:" + updataValue);
                        break;
                    case ADD://+/-N
                        updataValue = Integer.parseInt(sourceValue) + replaceCondition.getAddValue();
                        System.err.println("+/-N后:" + updataValue);
                        break;
                    case FIELDNAME://字段替换
                        updataValue = toString(docNoMap.get(replaceCondition.getTargetFieldName())).trim();
                        System.err.println("字段替换后:" + updataValue);
                        break;
                    case _NONE:
                    default:
                        b = false;
                        break;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            if (b) {
                docNoMap.put(replaceCondition.getSourceFieldName(), updataValue);
                return updataValue;
            }


        }
        return "";
    }
   /* @Override
    public JSONObject replace() throws Exception {
        String sql = "";
        boolean b = true;
        //判断是否是档号组成项
        String sqlStr = "";
        if (replaceCondition.getIsDocNoItem()) {
            sqlStr = " docNo = ? ";
        }


        switch (replaceCondition.getReplaceType()) {
            case PREFIX:
                sql = "UPDATE " + tableName + " SET "+ replaceCondition.getSourceFieldName() + "='" + replaceCondition.getNewValue() + "'+RTRIM(" + replaceCondition.getSourceFieldName() + ") WHERE " + keyFieldName + "=?";
                break;
            case SUFFIX:
                sql = "UPDATE " + tableName + " SET " + replaceCondition.getSourceFieldName() + "=RTRIM(" + replaceCondition.getSourceFieldName() + ")+'" + replaceCondition.getNewValue() + "' WHERE " + keyFieldName + "=?";
                break;
            case PARTIAL:
                sql = "UPDATE " + tableName + " SET " + replaceCondition.getSourceFieldName() + "=REPLACE(RTRIM(" + replaceCondition.getSourceFieldName() + "), '" + replaceCondition.getOldValue() + "', '" + replaceCondition.getNewValue() + "') WHERE " + keyFieldName + "=?";
                break;
            case ALL:
                sql = "UPDATE " + tableName + " SET " + replaceCondition.getSourceFieldName() + "='" + replaceCondition.getNewValue() + "' WHERE " + keyFieldName + "=?";
                break;
            case ADD:
                sql = "UPDATE " + tableName + " SET " + replaceCondition.getSourceFieldName() + "=" + replaceCondition.getSourceFieldName() + "+" + replaceCondition.getAddValue() + " WHERE " + keyFieldName + "=?";
                break;
            case FIELDNAME:
                sql = "UPDATE " + tableName + " SET " + replaceCondition.getSourceFieldName() + "=" + replaceCondition.getTargetFieldName() + " WHERE " + keyFieldName + "=?";
                break;
            case _NONE:
            default:
                b = false;
        }
        if (!b) {
            throw new Exception("替换类型不正确");
        }

        JSONArray datas = JSONArray.parseArray("[]");
        JSONObject retJson = JSONObject.parseObject("{}");
        JSONObject data;
        int ret;
        if (replaceCondition.getDataLists() != null && !replaceCondition.getDataLists().isEmpty()) {
            JSONArray array = replaceCondition.getDataLists();
            String docId;
            Connection conn = DbConnection.getConnection();
            List<String> params = new ArrayList<>();
            List<Map<String, Object>> values = new ArrayList<>();
            params.add(keyFieldName);
            Map<String, Object> map = new HashMap<>();
            try {
                conn.setAutoCommit(false);
                for (int i = 0; i < array.size(); i++) {
                    data = JSONObject.parseObject("{}");
                    docId = array.getString(i);
                    values.clear();
                    map.put(keyFieldName, docId);
                    values.add(map);
                    System.err.println(sql);
                    System.err.println(params);
                    System.err.println(values);
                    ret = DbUtility.execSQLWithTrans(conn, sql, params, values);
                    data.put("id", docId);
                    data.put("code", ret);
                    datas.add(data);
                }
                conn.commit();
                retJson.put("code", 0);
                retJson.put("data", datas);//要改
            } finally {
                conn.setAutoCommit(true);
                DbConnection.close(conn);
            }
        } else {//不传修改的档号ID 不允许修改
            *//*ret = DbUtility.execSQLWithTrans(sql);
            retJson.put("code", ret);*//*
            retJson.put("code", 1);
        }
        return retJson;
    }*/


    /**
     * 字符串工具
     *
     * @param obj
     * @return
     */
    public String toString(Object obj) {
        return (obj == null ? "" : obj.toString());
    }
}
