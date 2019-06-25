package com.tskj.fileimport.system.bean;

import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.bind.v2.model.core.ID;
import com.tskj.core.system.consts.DBConsts;
import com.tskj.docframe.dao.DocFrameManager;
import com.tskj.docno.bean.DocNoFieldInfo;
import com.tskj.docno.dao.DocNoDao;
import com.tskj.docno.impl.DocNoEngine;
import com.tskj.fileimport.system.Tools;
import com.tskj.fileimport.system.db.DbUtility;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import sun.plugin2.message.TextEventMessage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @notes: 字段关系的处理, 和临时表的操作
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-06 14:40
 **/
public class FieldRelation {

    private List<String> fieldNameList = new ArrayList<>();//保存的字段名集合
    private List<Map<String, Object>> fieldDetails = new ArrayList<>();//数据库对应字段的名称和长度 （字段属性集合）
    private String tempTableName;//临时档号表名称
    private String tempFieldDataTableName;//临时导入条目数据表名称
    private String targetKeyField;
    private String tempTalbeSql;
    private List<String> insertparams = new ArrayList();


    public void setTargetKeyField(String targetKeyField) {
        this.targetKeyField = targetKeyField;
    }

    public List<String> getFieldNameList() {
        return fieldNameList;
    }

    public List<Map<String, Object>> getFieldDetails() {
        return fieldDetails;
    }

    public String getTempTableName() {
        return tempTableName;
    }

    public String getTempFieldDataTableName() {
        return tempFieldDataTableName;
    }

    public FieldRelation(String targetKeyField, JSONObject assFields, String tableName, DocNoEngine docNoEngine, List<String> MandatoryField) throws Exception {
        //初始化验证档号完整性,获取所有需要上传字段的属性
        setTargetKeyField(targetKeyField);
        docNoIsFull(assFields, docNoEngine);
        getFieldDetails(tableName, MandatoryField);
    }

    //验证对应关系中 档号生成项是否完整
    public boolean docNoIsFull(JSONObject assFields, DocNoEngine docNoEngine) throws Exception {

        //获取对应门类档号生成规则

        Vector<DocNoFieldInfo> docNoRule = docNoEngine.getDocNoRule();
        int rel = 0;
        int size = docNoRule.size();
        for (String s : assFields.keySet()) {
            //先判断是档号组成项
            if (docNoEngine.indexOf(s)) {
                ++rel;
                //在判断是否是字典项，如果是字典字段，新增加字典ID字段名
                if (docNoEngine.indexOfDic(s)) {
                    fieldNameList.add(s + "_ID");
                }
            }
            //获取前台字段对应关系对应字段名
            fieldNameList.add(s);
        }
        //System.err.println(fieldNameList);
        if (rel != size) {
            throw new Exception("档号组成项传递不完整");
        }

        return true;
    }


    //获得字段的名称,长度,类型 TODO  没有档号，和没类ID 需要增加 如果有默认不动
    public void getFieldDetails(String tableName, List<String> MandatoryField) {
        DocNoDao docNoDao = new DocNoDao();
        //System.err.println("增加前:"+fieldNameList);
        //获取字段名称，长度类型
        List<Map<String, Object>> docframe = docNoDao.getFieldsSize(tableName);
        for (String s : MandatoryField) {
            boolean flag = false;
            //System.err.println("集合中是否存在字段:" + fieldNameList.contains(s));
            //不存在必填字段增加进去
            for (String s1 : fieldNameList) {
                if (s1.equalsIgnoreCase(s)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                fieldNameList.add(s);
            }
                /*if (!fieldNameList.contains(s)) {
                    fieldNameList.add(s);
                }*/
        }
        //System.err.println("增加后:"+fieldNameList);

        //添加字段对应关系
        for (String s : fieldNameList) {
            for (Map<String, Object> map : docframe) {
                //字段名相匹配添加map到字段属性集合
                //System.err.println(s);
                //System.err.println(map.get("fieldname").toString());
                if (s.equalsIgnoreCase(map.get("fieldname").toString())) {
                    fieldDetails.add(map);
                    break;
                }
            }
        }


    }


    //临时表创建

    /**
     * @param conn
     * @return
     * @Author JRX   * 保存docID，boxID，RoolID
     * @Description: ID 自增主键，DOCNO档号，index EXCAL中第几行数据，docIDname 什么类型档案库
     * @create 2019/3/7 15:17
     **/
    public void cTmpTable(Connection conn) throws Exception {
        //tempTableName = "##" + Tools.newId();
        //String TEMPORARY = "";
        String Pk = "";
        switch (com.tskj.core.db.DbUtility.getDBType()) {
            case DBConsts.DB_MYSQL:
                //TEMPORARY = " TEMPORARY ";
                tempTableName = "mySqlTemp" + Tools.newId();
                Pk = " ID  INT AUTO_INCREMENT PRIMARY KEY ";
                break;
            case DBConsts.DB_SQLSERVER:
                tempTableName = "##" + Tools.newId();
                Pk = " ID int identity(1,1) primary key ";
                break;
            case DBConsts.DB_ORACLE:
                break;
            default:
                break;
        }
        //创建临时关联表
        String sql = "CREATE  TABLE " + tempTableName + "(" + Pk + ",DOCNO VARCHAR(100),ROWINDEX int," + targetKeyField + " VARCHAR(32) )";
        //String sql = "CREATE " + TEMPORARY + " TABLE " + tempTableName + "(" + Pk + ",DOCNO VARCHAR(100),ROWINDEX int," + targetKeyField + " VARCHAR(32) )";
        //," + docIdName + "varchar(32)) ";
        JSONObject jsonObject = DbUtility.execSQL(conn, sql, null, null);
        if (!"0".equals(jsonObject.get("result").toString())) {
            throw new Exception("临时表创建失败!!!");
        }
        //System.err.println(jsonObject);
    }

    /**
     * @param conn
     * @param DataEx 其他字段 如 条目ID,等 （不能为空，最少要传docID）
     * @return
     * @Author JRX
     * @Description:
     * @create 2019/3/7 15:30
     **/
    public void cTmpDataTable(Connection conn, List<Map<String, Object>> DataEx) throws Exception {
        //JSONObject jsonObject = saveTmpDocNo(conn, "12313", 3123);
        //System.err.println(jsonObject);

        //String TEMPORARY = "";
        switch (com.tskj.core.db.DbUtility.getDBType()) {
            case DBConsts.DB_MYSQL:
                //TEMPORARY = " TEMPORARY ";
                tempFieldDataTableName = "mySqlTemp" + Tools.newId();
                break;
            case DBConsts.DB_SQLSERVER:
                tempFieldDataTableName = "##" + Tools.newId();
                break;
            case DBConsts.DB_ORACLE:
                break;
            default:
                break;
        }
        //创建临时条目保存表
        //StringBuilder createSQL = new StringBuilder("CREATE " + TEMPORARY + " TABLE " + tempFieldDataTableName + "(");
        StringBuilder createSQL = new StringBuilder("CREATE  TABLE " + tempFieldDataTableName + "(");
        for (Map<String, Object> fieldDetail : fieldDetails) {
            createSQL.append(fieldDetail.get("fieldname"));
            String fieldtype = fieldDetail.get("fieldtype").toString().toUpperCase();
            switch (fieldtype) {
                case "B":
                    createSQL.append(" VARCHAR(");
                    createSQL.append(fieldDetail.get("fieldsize"));
                    createSQL.append(")");
                    break;
                case "C":
                    createSQL.append(" CHAR(");
                    createSQL.append(fieldDetail.get("fieldsize"));
                    createSQL.append(")");
                    break;
                case "D":
                    createSQL.append(" VARCHAR(");
                    createSQL.append(fieldDetail.get("fieldsize"));
                    createSQL.append(")");
                    break;
                case "I":
                    createSQL.append(" INT");
                    break;
                case "V":
                    createSQL.append(" VARCHAR(");
                    createSQL.append(fieldDetail.get("fieldsize"));
                    createSQL.append(")");
                    break;
            }
            createSQL.append(",");
        }
        createSQL.deleteCharAt(createSQL.length() - 1);
        createSQL.append(")");
        //System.err.println(createSQL);
        JSONObject jsonObject = DbUtility.execSQL(conn, createSQL.toString(), null, null);
        if (!"0".equals(jsonObject.get("result").toString())) {
            throw new Exception("临时表创建失败!!!");
        }
    }


    //档号写入临时表  TODO 保存docid
    public JSONObject saveTmpDocNo(Connection conn, String DOCNO, int INDEX, String KeyFieldValue) {
        String sql = " INSERT INTO  " + tempTableName + "(DOCNO,ROWINDEX," + targetKeyField + ") VALUES('" + DOCNO + "'," + INDEX + ",'" + KeyFieldValue + "')";
        //System.err.println(sql);
        //System.err.println("写入临时表成功");
        return DbUtility.execSQL(conn, sql, null, null);
    }

    public void tempTableSqlInIt() {
        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tempFieldDataTableName + "(");
        StringBuilder valueSQL = new StringBuilder("VALUES (");
        for (String key : fieldNameList) {
            insertSQL.append(key);
            insertSQL.append(",");
            valueSQL.append("?,");
            insertparams.add(key);
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1);
        valueSQL.deleteCharAt(valueSQL.length() - 1);
        // 插入语句
        insertSQL.append(")");
        insertSQL.append(valueSQL);
        insertSQL.append(")");
        //System.err.println(insertSQL.toString());
        tempTalbeSql = insertSQL.toString();
    }

    //数据写入临时条目表
    public JSONObject saveTmpFields(Connection conn, Map<String, Object> tempValue) {
        List<Map<String, Object>> tempListValue = new ArrayList<>();
        tempListValue.add(tempValue);
       /* StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tempFieldDataTableName + "(");
        StringBuilder valueSQL = new StringBuilder("VALUES (");
        List<String> insertparams = new ArrayList<>();
        for (String key : fieldNameList) {
            insertSQL.append(key);
            insertSQL.append(",");
            valueSQL.append("?,");
            insertparams.add(key);
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1);
        valueSQL.deleteCharAt(valueSQL.length() - 1);
        // 插入语句
        insertSQL.append(")");
        insertSQL.append(valueSQL);
        insertSQL.append(")");*/

        //System.err.println(tempListValue);
        return DbUtility.execSQL(conn, tempTalbeSql, insertparams, tempListValue);
    }

    //

    /**
     * @param conn
     * @param tableName 目的表名
     * @param mode      0.跳过 min默认重复第一条  1.覆盖 max 默认重复最后一条
     * @return TODO sql有问题
     * @Author JRX
     * @Description: 不重复的临时数据数据按mode状态进行insert
     * @create 2019/3/8 11:41
     **/
    public JSONObject insertField(Connection conn, String tableName, int mode) {
        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + "(");
        StringBuilder valueSQL = new StringBuilder("");
        // 0.跳过 1.覆盖
        String sqlStr = "";
        if (mode == 0) {
            sqlStr = "MIN";
        } else {
            sqlStr = "MAX";
        }

        for (String key : fieldNameList) {
            insertSQL.append(key);
            insertSQL.append(",");
            valueSQL.append(key);
            valueSQL.append(",");
        }
        insertSQL.deleteCharAt(insertSQL.length() - 1);
        valueSQL.deleteCharAt(valueSQL.length() - 1);
        insertSQL.append(") SELECT ");
        insertSQL.append(valueSQL);

        insertSQL.append(" FROM " + tempFieldDataTableName + " WHERE EXISTS(SELECT * FROM " + tempTableName + " INNER JOIN (SELECT " + sqlStr + "(ID) AS CID FROM ");
        insertSQL.append(tempTableName + " WHERE NOT EXISTS (SELECT A.DOCNO FROM " + tableName + "  A WHERE A.DOCNO = " + tempTableName + ".DOCNO) GROUP BY DOCNO) ");
        insertSQL.append(" AS B ON B.CID = " + tempTableName + ".ID WHERE ");
        insertSQL.append(" " + tempFieldDataTableName + "." + targetKeyField + " = " + tempTableName + "." + targetKeyField + ")");
        /*StringBuilder b = new StringBuilder();
        b.append("SELECT * ");
        b.append(" FROM " + tempFieldDataTableName + " WHERE EXISTS(SELECT * FROM " + tempTableName + " INNER JOIN (SELECT " + sqlStr + "(ID) AS CID FROM ");
        b.append(tempTableName + " WHERE NOT EXISTS (SELECT A.DOCNO FROM " + tableName + "  A WHERE A.DOCNO = " + tempTableName + ".DOCNO) GROUP BY DOCNO) ");
        b.append(" AS B ON B.CID = " + tempTableName + ".ID WHERE ");
        b.append(" " + tempFieldDataTableName + "." + targetKeyField + " = " + tempTableName + "." + targetKeyField + ")");
        List<Map<String, Object>> list = DbUtility.execSQL(conn, b.toString(), null);*/
        //System.err.println(insertSQL);
        JSONObject jsonObject = DbUtility.execSQL(conn, insertSQL.toString(), null, null);
        return jsonObject;
    }

    //   TODO sql有问题
    //重复档号覆盖的
    public JSONObject repeatDocNoUpdatefield(Connection conn, String tableName) {


        /* DOCID BOXID ROOLID 统一都跳过（修改的不改原主键ID避免绑定的原件丢失）*/
        StringBuilder updateSQL = new StringBuilder("UPDATE " + tableName + " SET ");
        StringBuilder valueSQL = new StringBuilder("");
        for (String s : fieldNameList) {
            //跳过主键ID的修改
            if (!s.equalsIgnoreCase(targetKeyField)) {
                updateSQL.append(tableName);
                updateSQL.append(".");
                updateSQL.append(s);
                updateSQL.append(" = B.");
                updateSQL.append(s);
                updateSQL.append(",");
                valueSQL.append(s);
                valueSQL.append(",");
            }
        }
        updateSQL.deleteCharAt(updateSQL.length() - 1);
        valueSQL.deleteCharAt(valueSQL.length() - 1);
        updateSQL.append(" FROM " + tableName + ",(SELECT ");
        updateSQL.append(valueSQL);
        updateSQL.append(" FROM " + tempFieldDataTableName + " WHERE EXISTS ( SELECT * FROM " + tempTableName + " INNER JOIN (SELECT MAX(ID) AS CID FROM " + tempTableName);
        updateSQL.append(" WHERE EXISTS (SELECT A.DOCNO FROM " + tableName + " A WHERE A.DOCNO = " + tempTableName + ".DOCNO ) GROUP BY DOCNO) AS LIST ON LIST.CID = ");
        updateSQL.append(tempTableName + ".ID WHERE " + tempFieldDataTableName + "." + targetKeyField + " = " + tempTableName + "." + targetKeyField + ")) AS B WHERE ");
        updateSQL.append(tableName + ".DOCNO = B.DOCNO");


        //System.err.println(updateSQL);
        return DbUtility.execSQL(conn, updateSQL.toString(), null, null);
    }


    public List<Map<String, Object>> FindTempFieldDataTableName(Connection conn) {
        String sql = "SELECT * FROM " + tempFieldDataTableName + "";

        return DbUtility.execSQL(conn, sql, null);
    }

    public List<Map<String, Object>> FindTempTableName(Connection conn) {
        String sql = "SELECT * FROM " + tempTableName + "";
        return DbUtility.execSQL(conn, sql, null);
    }

    //重复跳过
    public void repeatSkip(Connection conn, String tableName) {
        String sql = "SELECT MIN(t.ID),t.DOCNO FROM (SELECT * FROM " + tempTableName + "  WHERE NOT EXISTS(SELECT  " + tableName + ".DOCNO FROM " + tableName + "  WHERE " + tableName + ".DOCNO = " + tempTableName + ".DOCNO)) AS t GROUP BY DOCNO )";
        //System.err.println(sql);
        List<Map<String, Object>> list = DbUtility.execSQL(conn, sql, null);
        //System.err.println(list);
        //System.err.println("能添加的实际条数:" + list.size());

    }

    //重复覆盖
    public void repeatCover(Connection conn, String tableName) {
        String sql = "SELECT MAX(t.ID),t.DOCNO FROM (SELECT * FROM " + tempTableName + "  WHERE NOT EXISTS(SELECT  " + tableName + ".DOCNO FROM " + tableName + "  WHERE " + tableName + ".DOCNO = " + tempTableName + ".DOCNO)) AS t GROUP BY DOCNO )";
        //System.err.println(sql);
        List<Map<String, Object>> list = DbUtility.execSQL(conn, sql, null);
        //System.err.println(list);
        //System.err.println("能添加的实际条数:" + list.size());
    }

    //用户选跳过重复 所得到的没有被导入的excal条目集合
    public List<Map<String, Object>> repeatSkipFields(Connection conn, String tableName) {
        StringBuilder findSql = new StringBuilder("SELECT * FROM ");
        findSql.append(tempTableName + " WHERE NOT EXISTS(SELECT * FROM(SELECT MIN(ID) AS CID FROM " + tempTableName);
        findSql.append(" WHERE NOT EXISTS(SELECT A.DOCNO FROM " + tableName + " A WHERE A.DOCNO = " + tempTableName + ".DOCNO ) GROUP BY DOCNO) AS AC ");
        findSql.append(" WHERE AC.CID = " + tempTableName + ".ID)");
        //System.err.println(findSql);
        List<Map<String, Object>> list = DbUtility.execSQL(conn, findSql.toString(), null);
        //System.err.println(list.size());
        return list;
    }

    public List<Map<String, Object>> repeatCoverFields(Connection conn, String tableName) {
        StringBuilder findSql = new StringBuilder("SELECT * FROM ");
        findSql.append(tempTableName + " WHERE NOT EXISTS(SELECT A.CID FROM(");
        findSql.append(" SELECT MAX(ID) AS CID,DOCNO FROM " + tempTableName + " WHERE NOT EXISTS(SELECT A.DOCNO FROM " + tableName + " A ");
        findSql.append(" WHERE A.DOCNO = " + tempTableName + ".DOCNO) GROUP BY DOCNO ");
        findSql.append(" UNION ALL ");
        findSql.append(" SELECT MAX(ID) AS CID,DOCNO FROM " + tempTableName + " WHERE  EXISTS(SELECT A.DOCNO FROM " + tableName + " A ");
        findSql.append(" WHERE A.DOCNO = " + tempTableName + ".DOCNO) GROUP BY DOCNO) AS A WHERE A.CID = " + tempTableName + ".ID)");
        //System.err.println(findSql);
        List<Map<String, Object>> list = DbUtility.execSQL(conn, findSql.toString(), null);
        return list;
    }

    //删除临时表

    public void dropTempTable(Connection conn) {
        String sql = "";
        String sql2 = "";

        switch (com.tskj.core.db.DbUtility.getDBType()) {
            case DBConsts.DB_MYSQL:
                sql = "drop table if exists " + tempTableName;
                sql2 = "drop table if exists " + tempFieldDataTableName;
                break;
            case DBConsts.DB_SQLSERVER:
                sql = "if object_id('tempdb.." + tempTableName + "') is not null Begin Drop table " + tempTableName + " End ";
                sql2 = "if object_id('tempdb.." + tempFieldDataTableName + "') is not null Begin Drop table " + tempFieldDataTableName + " End ";
                break;
            case DBConsts.DB_ORACLE:
                break;
            default:
                break;
        }

        //System.err.println(sql);
        //System.err.println(sql2);
        if (!"".equals(tempTableName)) {
            JSONObject jsonObject = DbUtility.execSQL(conn, sql, null, null);
            System.err.println(jsonObject);
        }
        if (!"".equals(tempFieldDataTableName)) {
            JSONObject jsonObject1 = DbUtility.execSQL(conn, sql2, null, null);
            System.err.println(jsonObject1);
        }

    }


}
