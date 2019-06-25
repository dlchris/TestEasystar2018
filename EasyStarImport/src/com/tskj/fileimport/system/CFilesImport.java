package com.tskj.fileimport.system;

import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;
import com.tskj.classtable.biz.ClassTableBiz;
import com.tskj.core.db.DbConnection;
import com.tskj.docframe.dao.DictManager;
import com.tskj.docno.bean.DocNoFieldInfo;
import com.tskj.docno.dao.DocNoDao;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.fileimport.system.bean.FieldRelation;
import com.tskj.fileimport.system.biz.ImportDataBiz;
import com.tskj.fileimport.system.bizimpl.ImportDataFactory;
import com.tskj.fileimport.system.consts.DataTypeConsts;
import com.tskj.fileimport.system.db.DbUtility;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-05 14:09
 **/
public class CFilesImport {
    private String updateTime = "";
    private FieldRelation fieldRelation;
    private DocNoEngine docNoEngine;
    private Connection conn = null;
    private List<String> docNoList = new ArrayList<>();
    private ClassTableBiz classTable;
    private JSONObject assFields;
    private String targetKeyField;
    private String tableName;

    public List<String> getDocNoList() {
        return docNoList;
    }

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
    private List<Map<String, Object>> DictValueList;
    private List<String> params = new ArrayList<>();

    public CFilesImport(String fileName, String path) {
        dataBiz = ImportDataFactory.getImortDataImpl(fileName, path);
    }

    public CFilesImport(ClassTableBiz classTable, String targetKeyField, String fileName, String path, JSONObject assFields, String tableName, String classType, String preFixDes, List<String> MandatoryField) throws Exception {
        System.err.println("初始化开始");
        dataBiz = ImportDataFactory.getImortDataImpl(fileName, path);
        this.classTable = classTable;
        this.targetKeyField = targetKeyField;
        this.assFields = assFields;
        this.tableName = tableName;
        this.docNoEngine = new DocNoEngine(tableName, classType, preFixDes);
        this.fieldRelation = new FieldRelation(targetKeyField, assFields, tableName, docNoEngine, MandatoryField);
        //获取档号组成项的字典项对应的DICTID
        DictManager dictManager = new DictManager();
        DictValueList = dictManager.getDict(tableName);
    }

    public void tempTableSqlInIt() {
        fieldRelation.tempTableSqlInIt();
    }


    public boolean verfiy(String fieldName) {
        return dataBiz.verfiy(fieldName);
    }

    public JSONObject verifyDataType(String sourceFieldName, DataTypeConsts fieldType, int fieldSize, String targetFieldName) {
        return dataBiz.verifyDataType(sourceFieldName, fieldType, fieldSize, targetFieldName);
    }

    public FieldRelation getFieldRelation() {
        return fieldRelation;
    }

    public void cTmpTable() throws Exception {
        fieldRelation.cTmpTable(conn);
    }

    public void cTmpDataTable(List<Map<String, Object>> DataEx) throws Exception {
        fieldRelation.cTmpDataTable(conn, DataEx);
    }


    /**
     * 删除上传的文件
     */
    public void delFile() {
        if (dataBiz != null) {
            dataBiz.deleteFile();
        }
    }

    public JSONObject save(JSONObject keyFieldName, Map<String, Object> dataEx, int index,
                           int count) {
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        if (null == keyFieldName) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "主键不能为空");
            return jsonReturn;
        }
        //获取当前行用户选中主键名
        /*long ltime = System.currentTimeMillis();
        long startTime = ltime;*/
        //String keyName = keyFieldName.getString(keyFieldName.keySet().toArray()[0].toString());

        //List<String> cols = dataBiz.getColumns();
        //获得当前行对应数据
        Map<String, Object> dataMap = dataBiz.getData(index);

        //System.err.println(keyName);
        /*String keyValue = "";
        for (String colName : cols) {
            // 逐列循环
            if (colName.equalsIgnoreCase(keyName)) {
                keyValue = dataMap.get(colName).toString();
                break;
            }
        }*/

        if (null == dataBiz) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "未知文件类型");
            jsonReturn.put("rowCount", count);
            jsonReturn.put("rowIndex", index);
            //jsonReturn.put("KEYS", keyValue);
            jsonReturn.put("KEYRESULT", "失败");
            return jsonReturn;
        }
//        try {

        if (dataMap == null) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", "不能读取xls文件");

            return jsonReturn;
        }
        updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(System.currentTimeMillis());

        //数据集合
        Map<String, Object> fieldMap = new HashMap<>();


        try {
            //获得数据配对集合
            fieldMap = keyValuePair(dataMap, assFields);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("字段集合解析出错：" + e.getMessage());
        }

        //System.err.println(fieldMap);
        //集合加入字典项字段值  TODO 时间长
        Map<String, Object> newFieldMap = addDicFieldValue(fieldMap);
        //System.err.println(newFieldMap);
        //生成档号
        JSONObject jsonstr = new JSONObject(newFieldMap);
        String DocNo = docNoEngine.getNewDocNoByJson(jsonstr, true);
        //docNoList.add(DocNo);
        //System.err.println("档号:" + DocNo);
        //强制设置主键列为需导入的列，excal中的ID不要 自行生成UUID,因为他的有可能是1,2,3,4,5..... 不唯一
        //添加对应的主键ID名
        String targetKeyField = classTable.getKeyFieldName().toUpperCase();
        String keyfieldValue = Tools.newId();
        fieldMap.put(targetKeyField, keyfieldValue);
        fieldMap.put("DOCNO", DocNo);


        //保存档号到临时表
        JSONObject jsonObject = fieldRelation.saveTmpDocNo(conn, DocNo, index, keyfieldValue);
        if (jsonObject.getInteger("result") != 0) {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", jsonObject.getString("errmsg"));
            jsonReturn.put("rowCount", count);
            jsonReturn.put("rowIndex", index);
            //jsonReturn.put("KEYS", keyValue);
            jsonReturn.put("KEYRESULT", "导入临时表失败");
            return jsonReturn;
        }
        //保存条目数据到条目临时表
        //System.err.println("每条导入的数据:" + fieldMap);
        JSONObject ret = fieldRelation.saveTmpFields(conn, fieldMap);
        /*long endTime = System.currentTimeMillis();
        endTime = endTime;
        System.err.println("对应关系耗时测试:" + (endTime - startTime)+"ms");*/
        if (ret.getInteger("result") == 0) {
            jsonReturn.put("result", 0);
            jsonReturn.put("errMsg", "导入临时表成功");
            jsonReturn.put("updateTime", updateTime);
            jsonReturn.put("rowCount", count);
            jsonReturn.put("rowIndex", index);
            //jsonReturn.put("KEYS", keyValue);
            jsonReturn.put("KEYRESULT", "成功");
            return jsonReturn;
        } else {
            jsonReturn.put("result", 1);
            jsonReturn.put("errMsg", ret.getString("errmsg"));
            jsonReturn.put("rowCount", count);
            jsonReturn.put("rowIndex", index);
            //jsonReturn.put("KEYS", keyValue);
            jsonReturn.put("KEYRESULT", "导入临时表失败");
            return jsonReturn;
        }
    }

    //获取对应档号字典ID 并存入传递list集合
    public Map<String, Object> addDicFieldValue(Map<String, Object> fieldMap) {
       /* //获取档号组成项的字典项对应的DICTID
        DictManager dictManager = new DictManager();
        //TODO 变成初始化 减少时间
        List<Map<String, Object>> list = dictManager.getDict(tableName);*/
        //档号组成项的字典项
        List<Map<String, Object>> dictFields = classTable.getDictFields();

        for (Map<String, Object> dictField : dictFields) {
            Map<String, Object> mapEx = new HashMap<>();
            //是字典项   TODO改变方式增加速度
            if (fieldMap.containsKey(dictField.get("FIELDNAME"))) {
                String dictid = "";
                String dictKey = dictField.get("FIELDNAME") + "_ID";
                //加入list<String>集合
                //params.add(dictKey);
                for (Map<String, Object> map : DictValueList) {
                    if (map.containsValue(fieldMap.get(dictField.get("FIELDNAME")))) {
                        dictid = map.get("DICTID").toString();
                        break;
                    } else {

                    }
                }
                //长度不为空
                if (!"".equals(dictid)) {// todo 档号组成项字典项ID为空时保存有问题
                    mapEx.put(dictKey, dictid);
                    //System.err.println(dictKey+"---"+dictid);
                } else {//为空通过字典项长度生成一个ID 如：0000 (改到档号生成中)

                    //String FIELDSIZE = dictField.get("IDSIZE").toString();
                    //String tmpStr = String.format("%0" + FIELDSIZE + "d", 0);
                    mapEx.put(dictKey, "");
                }
            } else {
                //不是字典项跳过(应该没有该情况)
            }
            fieldMap.putAll(mapEx);
        }
        return fieldMap;
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
     * //* @param mode      0.跳过 1.覆盖
     *
     * @return
     * @Author JRX
     * @Description:
     * @create 2019/2/25 10:28
     **/
    public JSONObject saveToDB(int mode) {
        if (mode == 0) {
            //System.err.println("跳过");
            long l = System.currentTimeMillis();
            JSONObject jsonObject = fieldRelation.insertField(conn, tableName, mode);
            long l2 = System.currentTimeMillis();
            System.err.println("只新增所需时间:" + (l2 - l));
            return jsonObject;
        } else {
            //System.err.println("覆盖");
            //先做覆盖处理在把需要新增的条目写入（否则先新增后，修改的update又要吧新增的在update一遍）
            long l = System.currentTimeMillis();
            JSONObject jsonObject = fieldRelation.repeatDocNoUpdatefield(conn, tableName);
            long l2 = System.currentTimeMillis();
            System.err.println("覆盖所需时间:" + (l2 - l));
            if (jsonObject.getInteger("result") == 0) {
                long l3 = System.currentTimeMillis();
                jsonObject = fieldRelation.insertField(conn, tableName, mode);
                long l4 = System.currentTimeMillis();
                System.err.println("覆盖新增所需时间:" + (l4 - l3));
                return jsonObject;
            }
            return jsonObject;
        }
    }

    //选择覆盖 即将不会操作的条目
    public List<Map<String, Object>> repeatCoverFields() {
        return fieldRelation.repeatCoverFields(conn, tableName);
    }

    //选择跳过 即将不会操作的条目
    public List<Map<String, Object>> repeatSkipFields() {
        return fieldRelation.repeatSkipFields(conn, tableName);
    }

    //删除临时表
    public void dropTempTable() {
        fieldRelation.dropTempTable(conn);
    }

    public void findTempList() {
        List<Map<String, Object>> list = fieldRelation.FindTempFieldDataTableName(conn);
        if (list != null && !list.isEmpty()) {
            //System.err.println(list.get(0));
        }
        List<Map<String, Object>> list1 = fieldRelation.FindTempTableName(conn);

        //System.err.println(list1);
    }


}
