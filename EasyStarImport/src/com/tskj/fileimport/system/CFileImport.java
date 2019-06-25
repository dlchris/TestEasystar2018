package com.tskj.fileimport.system;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.biz.ClassTableBiz;
import com.tskj.core.db.DbConnection;
import com.tskj.docframe.dao.DictManager;
import com.tskj.docno.bean.DocNoFieldInfo;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.fileimport.system.biz.ImportDataBiz;
import com.tskj.fileimport.system.bizimpl.ImportDataFactory;
import com.tskj.fileimport.system.consts.DataTypeConsts;
import com.tskj.fileimport.system.db.DbUtility;


/**
 * <p>
 * Title:FileImport
 * </p>
 * <p>
 * Description:数据导入的后台函数
 * </p>
 *
 * @author LeonSu, DuXiao
 * @date 2017年12月19日
 */
public class CFileImport {

    private String updateTime = "";

    private Connection conn = null;

    public void startTrans() {
        if (null == conn) {
            conn = DbConnection.getConnection();
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void endTrans(boolean commit) {
        try {
            if (commit) {
                conn.commit();
            } else {
                if (!conn.getAutoCommit()) {
                    conn.rollback();
                }
            }
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private ImportDataBiz dataBiz;

    public CFileImport(String fileName, String path) {
        dataBiz = ImportDataFactory.getImortDataImpl(fileName, path);
    }

    public boolean verfiy(String fieldName) {
        return dataBiz.verfiy(fieldName);
    }

    public JSONObject verifyDataType(String sourceFieldName, DataTypeConsts fieldType, int fieldSize, String targetFieldName) {
        return dataBiz.verifyDataType(sourceFieldName, fieldType, fieldSize, targetFieldName);
    }

    /**
     * 删除上传的文件
     */
    public void delFile() {
        if (dataBiz != null) {
            dataBiz.deleteFile();
        }
    }

    /**
     * MethodName:saveMaterial
     * <p>
     * Description:将EXCl中的内容保存到数据库表中
     * </p>
     *
     * @param mode         发现重复数据时，0：覆盖；1：跳过；2：中止
     * @param tableName    要导入的表名
     * @param keyFieldName JSONObject, 目的表主键的字段名及对应的来源表的字段名，比如：{"sourcefield":"DOCID", "targetfield":"档案ID"}
     * @param assList      JSONArray，targetfield为目的字段 ，sourcefield为源字段。比如：[{"sourcefield":"DOCID", "targetfield":"档案ID"}]
     * @param dataEx       扩展的数据，此变量中的值会做为系统用的字段的赋值，比如：ISDEL=0等
     * @param index        导入第几条
     * @param count        总数
     * @return 保存的状态 真表示保存成功
     * @author 苏鹏
     */
    public JSONObject save(ClassTableBiz classTable, int mode, String tableName,
                           JSONObject keyFieldName,
                           JSONObject assList, Map<String, Object> dataEx, int index,
                           int count) {
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        //classTable.getDictFields()
        if (null == keyFieldName) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "主键不能为空");
            return jsonReturn;
        }
        String keyName = keyFieldName.getString(keyFieldName.keySet().toArray()[0].toString());
        String keyValue = "";
        if (null == dataBiz) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "未知文件类型");
            jsonReturn.put("rowIndex", index);
            jsonReturn.put("rowCount", count);
            jsonReturn.put("KEYS", keyValue);
            jsonReturn.put("KEYRESULT", "失败");
            return jsonReturn;
        }
//        try {
        Map<String, Object> dataMap = dataBiz.getData(index);
        if (dataMap == null) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "不能读取xls文件");

            return jsonReturn;
        }
        List<String> cols = dataBiz.getColumns();
        updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(System.currentTimeMillis());
        /*
         * 如果主键列不包含在需要导入的列中，则强制设置主键列在需导入的列中
         * 强制设置主键列为需导入的列
         */

        for (String colName : cols) {
            // 逐列循环

            if (colName.equalsIgnoreCase(keyName)) {
                keyValue = dataMap.get(colName).toString();
                if (!assList.containsValue(colName)) {
                    dataMap.put(colName, keyValue);
                }
                break;
            }
        }

        //补充字典项ID
        /*
        根据dataMap和AssList重新生成一个新的dataMap
        原dataMap的格式是：全宗号=XXXX，年度=XXXX，AAAA=XXXX，BBBB=XXXX
        新dataMap的格式是：ALLNO=XXXX，YEARNO=XXXX，AAAA=XXXX
         */

        JSONObject ret = saveToDB(conn, tableName, keyFieldName,
                dataMap, assList, dataEx);
        if (ret.getInteger("result") == 0) {
            jsonReturn.put("result", 0);
            jsonReturn.put("errMsg", "导入成功");
            jsonReturn.put("updateTime", updateTime);
            jsonReturn.put("rowIndex", index);
            jsonReturn.put("rowCount", count);
            jsonReturn.put("KEYS", keyValue);
            jsonReturn.put("KEYRESULT", "成功");
            return jsonReturn;
        } else {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", ret.getString("errmsg"));
            jsonReturn.put("rowIndex", index);
            jsonReturn.put("rowCount", count);
            jsonReturn.put("KEYS", keyValue);
            jsonReturn.put("KEYRESULT", "失败");
            return jsonReturn;
        }
    }

    public JSONObject save(ClassTableBiz classTable, int mode, String tableName, JSONObject keyFieldName, JSONObject assList, Map<String, Object> dataEx, int index,
                           int count, DocNoEngine docNoEngine) {

        JSONObject jsonReturn = JSONObject.parseObject("{}");
        if (null == keyFieldName) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "主键不能为空");
            return jsonReturn;
        }
        String keyName = keyFieldName.getString(keyFieldName.keySet().toArray()[0].toString());
        String keyValue = "";
        if (null == dataBiz) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "未知文件类型");
            jsonReturn.put("rowIndex", index);
            jsonReturn.put("rowCount", count);
            jsonReturn.put("KEYS", keyValue);
            jsonReturn.put("KEYRESULT", "失败");
            return jsonReturn;
        }
//        try {
        Map<String, Object> dataMap = dataBiz.getData(index);
        if (dataMap == null) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "不能读取xls文件");

            return jsonReturn;
        }
        List<String> cols = dataBiz.getColumns();
        updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(System.currentTimeMillis());
        /*
         * 如果主键列不包含在需要导入的列中，则强制设置主键列在需导入的列中
         * 强制设置主键列为需导入的列
         * TODO excal中的ID不要 自行生成UUID,因为他的有可能是1,2,3,4,5..... 不唯一
         */
        //数据集合
        Map<String, Object> fieldMap = new HashMap<>();
        //字段名集合（过时不用了）
        List<String> params = new ArrayList<>();

        try {
            //获得数据配对集合
            fieldMap = keyValuePair(dataMap, assList);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("字段集合解析出错：" + e.getMessage());
        }
        //获取档号组成项的字典项对应的DICTID
        DictManager dictManager = new DictManager();
        List<Map<String, Object>> list = dictManager.getDict(tableName);
        //档号组成项的字典项
        List<Map<String, Object>> dictFields = classTable.getDictFields();
        Map<String, Object> mapEx = new HashMap<>();
        for (Map<String, Object> dictField : dictFields) {
            //是字典项
            if (fieldMap.containsKey(dictField.get("FIELDNAME"))) {
                String dictid = "";
                String dictKey = dictField.get("FIELDNAME") + "_ID";
                //加入list<String>集合
                //params.add(dictKey);
                for (Map<String, Object> map : list) {
                    if (map.containsValue(fieldMap.get(dictField.get("FIELDNAME")))) {
                        dictid = map.get("DICTID").toString();
                        break;
                    } else {

                    }
                }
                //长度不为空
                if (!"".equals(dictid)) {// todo 档号组成项字典项ID为空时保存有问题
                    mapEx.put(dictKey, dictid);
                } else {//为空通过字典项长度生成一个ID 如：0000 (改到档号生成中)

                    //String FIELDSIZE = dictField.get("IDSIZE").toString();
                    //String tmpStr = String.format("%0" + FIELDSIZE + "d", 0);
                    mapEx.put(dictKey, "");
                }
            } else {
                //不是字典项跳过(应该没有该情况)
            }
        }
        fieldMap.putAll(mapEx);
        JSONObject jsonstr = new JSONObject(fieldMap);
        String DocNo = docNoEngine.getNewDocNoByJson(jsonstr, true);
        System.err.println(DocNo);
        /*//判断DocNo是否存在,不存在增加一个
        if (!params.contains("DOCNO")) {
            params.add("DOCNO");
        }*/


        //添加对应的主键ID名
        String targetKeyField = classTable.getKeyFieldName();
        fieldMap.put(targetKeyField, Tools.newId());
        //params.add(targetKeyField);
        //补充字典项ID
        /*
        根据dataMap和AssList重新生成一个新的dataMap
        原dataMap的格式是：全宗号=XXXX，年度=XXXX，AAAA=XXXX，BBBB=XXXX
        新dataMap的格式是：ALLNO=XXXX，YEARNO=XXXX，AAAA=XXXX
         */

        JSONObject DocNoValue = new JSONObject();
        DocNoValue.put("DOCNO", DocNo);
        JSONObject ret = saveToDB(mode, conn, tableName, DocNoValue,
                fieldMap, params, dataEx);
        if (ret.getInteger("result") == 0) {
            jsonReturn.put("result", 0);
            jsonReturn.put("errMsg", "导入成功");
            jsonReturn.put("updateTime", updateTime);
            jsonReturn.put("rowIndex", index);
            jsonReturn.put("rowCount", count);
            jsonReturn.put("KEYS", keyValue);
            jsonReturn.put("KEYRESULT", "成功");
            return jsonReturn;
        } else {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", ret.getString("errmsg"));
            jsonReturn.put("rowIndex", index);
            jsonReturn.put("rowCount", count);
            jsonReturn.put("KEYS", keyValue);
            jsonReturn.put("KEYRESULT", "失败");
            return jsonReturn;
        }
    }

    //键值对配对
    private Map<String, Object> keyValuePair(Map<String, Object> dataMap, JSONObject assList) throws Exception {
        Map<String, Object> fieldMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : assList.entrySet()) {
            if (dataMap.containsKey(entry.getValue())) {
                //导入数据存入map
                fieldMap.put(entry.getKey(), dataMap.get(entry.getValue()) == null ? "" : dataMap.get(entry.getValue()));

                //params.add(entry.getKey());

            } else {
                //传值有误
                //System.err.println("dataMap中不包含：" + entry.getKey());
            }

        }
        return fieldMap;
    }


    /**
     * @param tableName 表名
     * @param DocNo     档号
     * @return
     * @Author JRX
     * @Description: 判断档号是否重复
     * @create 2019/2/20 15:42
     **/
    private boolean isRepeat(String tableName, String DocNo) {
        String sql = "select * from '" + tableName + "' where DocNo = '" + DocNo + "' ";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);

        if (list != null && list.size() > 0) {
            return true;//不为空  有重复
        }
        return false;//无重复
    }

    /***
     * @Author JRX
     * @Description: 检测到空行之前的数据总条数
     * @create 2018/11/15 12:35
     * @Param
     * @return
     **/
    public int getNotEmptyRowCount() {
        if (dataBiz == null) {
            return 0;
        }
        return dataBiz.getSizeCount();
    }


    /**
     * MethodName:readMaterial
     * <p>
     * Description:返回excl中字段名的集合
     * </p>
     *
     * @return List<String> 列集合
     * @author DuXiao
     */
    public List<String> getColumns() {
        if (dataBiz == null) {
            return null;
        }
        return dataBiz.getColumns();
    }

    /**
     * MethodName:readMaterial
     * <p>
     * Description:返回excl中字段名的集合
     * </p>
     *
     * @return excel文件中的记录总数
     * @author DuXiao
     */
    public int getRowCount() {
        if (dataBiz == null) {
            return 0;
        }
        return dataBiz.getSize();
    }

    /**
     * @param mode       0.跳过 1.覆盖
     * @param conn
     * @param tableName  表名
     * @param DocNoValue 档号DocNo 如：DocNo:"2056-555-y-596"
     * @param listSheet  数据集合
     * @param params     字段集合（过时不用了）
     * @param dataEx
     * @return
     * @Author JRX
     * @Description:
     * @create 2019/2/25 10:28
     **/
    private JSONObject saveToDB(int mode, Connection conn, String tableName,
                                JSONObject DocNoValue,
                                Map<String, Object> listSheet, List<String> params,
                                Map<String, Object> dataEx) {
        JSONObject jsonResult = JSONObject.parseObject("{}");
        List<Map<String, Object>> tempValue = new ArrayList<>();
        List<String> updateparams = new ArrayList<>();
        List<String> insertparams = new ArrayList<>();
        // Connection conn = null;
        String selectItem;
        // 改版后 targetKeyName是DocNo sourceKeyName是档号值
        String targetKeyName = DocNoValue.keySet().toArray()[0].toString();
        String sourceKeyName = DocNoValue.get(targetKeyName).toString();

        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + "(");

        StringBuilder updateSQL = new StringBuilder("UPDATE " + tableName + " SET ");

        StringBuilder valueSQL = new StringBuilder("VALUES (");
        Set<String> keys = (Set<String>) listSheet.keySet();
        for (String key : keys) {
            insertSQL.append(key);
            insertSQL.append(",");
            valueSQL.append("?,");
            updateSQL.append(key);
            updateSQL.append("= ?,");
            updateparams.add(key);
            insertparams.add(key);
        }

        // 对扩展属性（系统级的字段）的处理
        if (dataEx != null) {
            for (Map.Entry<String, Object> entry : dataEx.entrySet()) {
                insertSQL.append(entry.getKey());
                insertSQL.append(",");
                valueSQL.append("?,");
                updateSQL.append(entry.getKey());
                updateSQL.append("= ?,");
                updateparams.add(entry.getKey());
                insertparams.add(entry.getKey());
            }
        }
        updateSQL.deleteCharAt(updateSQL.length() - 1);
        updateparams.add(targetKeyName);
        insertSQL.deleteCharAt(insertSQL.length() - 1);
        valueSQL.deleteCharAt(valueSQL.length() - 1);

        // 插入语句
        insertSQL.append(")");
        insertSQL.append(valueSQL);
        insertSQL.append(")");

        // 更新语句
        updateSQL.append(" WHERE ");
        updateSQL.append(targetKeyName);
        updateSQL.append("=?");
        selectItem = "SELECT 1 FROM " + tableName + " WHERE " + targetKeyName
                + "= ?";
        // 判断更新还是插入的标记 tag=true为更新语句
        Boolean tag;
        try {
            Map<String, Object> value;
            // 逐行操作
            String keyValue = sourceKeyName;
            tempValue.clear();
            value = listSheet;

            if (dataEx != null) {
                for (Map.Entry<String, Object> entry : dataEx.entrySet()) {
                    value.put(entry.getKey(), entry.getValue());
                }
            }

            tempValue.add(value);
            // 一定是更新语句
            if (keyValue == null || keyValue.isEmpty()) {
                tag = false;
            } else {
                String[] paramStr = {keyValue};
                tag = DbUtility.execSQL(conn, selectItem, paramStr)
                        .size() > 0;
            }
            JSONObject ret = new JSONObject();
            if (tag) {
                System.err.println("修改");
                if (mode == 1) {
                    ret = DbUtility.execSQL(conn,
                            updateSQL.toString(), updateparams, tempValue);
                    System.err.println(ret);
                } else {
                    ret.put("result", "0");
                    ret.put("errmsg", "跳过该条目");
                }

            } else {
                //添加
                System.err.println("添加");
                ret = DbUtility.execSQL(conn,
                        insertSQL.toString(), insertparams, tempValue);
            }
            jsonResult.put("result", ret.get("result"));
            if (ret.containsKey("errmsg")) {
                jsonResult.put("errmsg", ret.get("errmsg"));
            }
        } finally {
            return jsonResult;
        }


    }


    /**
     * MethodName:saveToDB
     * <p>
     * Description:
     * </p>
     *
     * @param tableName    要导入的表名
     * @param keyFieldName 目的表主键的字段名及对应的来源表的字段名，比如：DOCID=档案ID
     * @param listSheet    数据
     * @param map          数据字段的对应关系
     * @param dataEx       扩展的数据，此变量中的值会做为系统用的字段的赋值，比如：ISDEL=0，DOCID=XXXXXX等
     * @return 数据是否已导入成功
     * @author DuXiao
     */
    private JSONObject saveToDB(Connection conn, String tableName,
                                JSONObject keyFieldName,
                                Map<String, Object> listSheet, JSONObject map,
                                Map<String, Object> dataEx) {
        JSONObject jsonResult = JSONObject.parseObject("{}");
        List<Map<String, Object>> tempValue = new ArrayList<>();
        List<String> updateparams = new ArrayList<>();
        List<String> insertparams = new ArrayList<>();
        // Connection conn = null;
        String selectItem;
        String targetKeyName = keyFieldName.keySet().toArray()[0].toString();
        String sourceKeyName = keyFieldName.get(targetKeyName).toString();

        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + "(");
//				+ " (updatetype");
        StringBuilder updateSQL = new StringBuilder("UPDATE " + tableName + " SET ");
//				+ " SET updatetype=0,");
        StringBuilder valueSQL = new StringBuilder("VALUES (");
        Set<String> keys = (Set<String>) map.keySet();
        for (String key : keys) {
            insertSQL.append(key);
            insertSQL.append(",");
            valueSQL.append("?,");
            updateSQL.append(key);
            updateSQL.append("= ?,");
            updateparams.add(map.getString(key));
            insertparams.add(map.getString(key));
        }

        // 对扩展属性（系统级的字段）的处理
        if (dataEx != null) {
            for (Map.Entry<String, Object> entry : dataEx.entrySet()) {
                insertSQL.append(entry.getKey());
                insertSQL.append(",");
                valueSQL.append("?,");
                updateSQL.append(entry.getKey());
                updateSQL.append("= ?,");
                updateparams.add(entry.getKey());
                insertparams.add(entry.getKey());
            }
        }
        updateSQL.deleteCharAt(updateSQL.length() - 1);
        updateparams.add(map.getString(targetKeyName));
        insertSQL.deleteCharAt(insertSQL.length() - 1);
        valueSQL.deleteCharAt(valueSQL.length() - 1);

        // 插入语句
        insertSQL.append(")");
        insertSQL.append(valueSQL);
        insertSQL.append(")");

        // 更新语句
        updateSQL.append(" WHERE ");
        updateSQL.append(targetKeyName);
        updateSQL.append("=?");
        selectItem = "SELECT 1 FROM " + tableName + " WHERE " + targetKeyName
                + "= ?";

        // 判断更新还是插入的标记 tag=true为更新语句
        Boolean tag;
        try {
            Map<String, Object> value;
            // 逐行操作
            String keyValue = listSheet.get(sourceKeyName)
                    .toString();
            tempValue.clear();
            value = listSheet;

            if (dataEx != null) {
                for (Map.Entry<String, Object> entry : dataEx.entrySet()) {
                    value.put(entry.getKey(), entry.getValue());
                }
            }

            tempValue.add(value);
            // 一定是更新语句
            if (keyValue == null || keyValue.isEmpty()) {
                tag = false;
            } else {
                String[] params = {keyValue};
                tag = DbUtility.execSQL(conn, selectItem, params)
                        .size() > 0;
            }

            JSONObject ret;
            if (tag) {
                ret = DbUtility.execSQL(conn,
                        updateSQL.toString(), updateparams, tempValue);
            } else {
                ret = DbUtility.execSQL(conn,
                        insertSQL.toString(), insertparams, tempValue);
            }
            jsonResult.put("result", ret.get("result"));
            if (ret.containsKey("errmsg")) {
                jsonResult.put("errmsg", ret.get("errmsg"));
            }
        } finally {
            return jsonResult;
        }
    }
}
