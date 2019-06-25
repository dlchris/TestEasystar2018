package com.tskj.classtree.dao;

import com.alibaba.fastjson.JSONObject;
import com.tskj.classtree.bean.ClassTreeInfo;
import com.tskj.core.db.DbUtility;
import com.tskj.core.system.consts.DBConsts;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LeonSu
 */
public class ClassTree {

    /**
     * 获取档案门类
     *
     * @param classType 0:普通档案门类；1：其他档案库
     * @return
     */
    public JSONObject getClassTree(int classType) {
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        // todo * 改成 CLASSID,DESCRIPTION,CLASSTYPE
        String sql = "SELECT CLASSID,DESCRIPTION,CLASSTYPE FROM CLASSTREE WHERE CLASSLEVEL=7 ";
        switch (classType) {
            case 0:
                sql += " AND (CLASSTYPE=0 OR CLASSTYPE=1) ";
                break;
            case 1:
                sql += " AND CLASSTYPE=3";
                break;
            default:
                jsonReturn.put("result", 1);
                jsonReturn.put("error", "档案门类类型不正确，0:普通档案门类；1：其他档案库");
                return jsonReturn;
        }
        sql += "ORDER BY DESCRIPTION";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list == null) {
            jsonReturn.put("result", 1);
            jsonReturn.put("error", "获取门类失败");
        } else {
            jsonReturn.put("result", 0);
            jsonReturn.put("values", list);
        }
        return jsonReturn;
    }

    /**
     * 根据当前Sessrion，对所有可操作的档案库分别产生一个临时ID，保存到内存数据库中，有效期是24小时
     * 每个用户或角色，每次产生的临时ID都不一样
     *
     * @param userId 当前Session
     * @return 包含临时档案库ID的档案库列表
     */
//    public List<Map<String, Object>> getClassTree(String userId, String roleId) {
//        List<Map<String, Object>> classTree = getClassTree();
//        return classTree;
//    }
    public List<Map<String, Object>> getClassTree(String userId, String roleId) {
        String sql;
//        String userId = "";
//        String roleId = "";
        List<Map<String, Object>> classTree;
        switch (DbUtility.getDBType()) {
            case DBConsts.DB_SQLSERVER: //sqlServer text类型字段用 CAST(SECURITY AS VARCHAR(max)) <> ''   cast转换后判断为空
                sql = "SELECT REPLACE(NEWID(), '-', '') as 'newClassId', CLASSID, DESCRIPTION, PERFIXDES, DOCTABLE, BOXTABLE, ROOLTABLE, dbo.GetClassType(DOCTABLE, BOXTABLE, ROOLTABLE) as 'CLASSTYPE' FROM CLASSTREE WHERE CLASSLEVEL='7' AND CLASSTYPE=1 ";
                if (!userId.isEmpty()) {//!roleId.isEmpty() &&
                    sql += "and (classid in (select moduleid from rolemodule where (CAST(SECURITY AS VARCHAR(max)) <> '' or CAST(SECURITY2 AS VARCHAR(max)) <> '')  and  roleid='" + roleId + "' and exists(select 1 from classtree where classid=moduleid)) or classid in (select moduleid from usermodule where (CAST(SECURITY AS VARCHAR(max)) <> '' or CAST(SECURITY2 AS VARCHAR(max)) <> '') and  userid='" + userId + "' and exists(select 1 from classtree where classid=moduleid))) ";
                }
                sql += "ORDER BY DESCRIPTION";
                break;
            case DBConsts.DB_MYSQL: //MYsql text类型字段用 SECURITY <> ''判断为空
                // TODO dbo.GetClassType 没修改
                sql = "SELECT REPLACE(UUID(), '-', '') as 'newClassId', CLASSID, DESCRIPTION, PERFIXDES, DOCTABLE, BOXTABLE, ROOLTABLE, GetClassType(DOCTABLE, BOXTABLE, ROOLTABLE) as 'CLASSTYPE' FROM CLASSTREE WHERE CLASSLEVEL='7' AND CLASSTYPE=1 ";
                if (!userId.isEmpty()) {//!roleId.isEmpty() &&
                    sql += "and (classid in (select moduleid from rolemodule where  (SECURITY <> '' or SECURITY2 <> '') and roleid='" + roleId + "' and exists(select 1 from classtree where classid=moduleid)) or classid in (select moduleid from usermodule where (SECURITY <> '' or SECURITY2 <> '') and userid='" + userId + "' and exists(select 1 from classtree where classid=moduleid))) ";
                }
                sql += "ORDER BY DESCRIPTION";
                break;
            case DBConsts.DB_ORACLE:
                sql = "SELECT 1";
                break;
            default:
                return null;
        }
        //System.err.println(sql);
        classTree = DbUtility.execSQL(sql);
        return classTree;
    }

    /**
     * 增加档案门类
     *
     * @param classTreeInfo
     * @return
     */
    public JSONObject add(ClassTreeInfo classTreeInfo) {
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        if (null == classTreeInfo) {
            jsonReturn.put("result", 1);
            jsonReturn.put("error", "没有有效的门类信息");
            return jsonReturn;
        }
        String sql = "SELECT 1 FROM CLASSTREE WHERE CLASSID='" + classTreeInfo.getRealClassId() + "' OR DESCRIPTION='" + classTreeInfo.getDescription() + "'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list == null) {
            jsonReturn.put("result", 1);
            jsonReturn.put("error", "重复检查失败");
            return jsonReturn;
        }
        if (list.size() > 0) {
            jsonReturn.put("result", 1);
            jsonReturn.put("error", "门类ID或描述重复");
            return jsonReturn;
        }
        sql = "INSERT INTO CLASSTREE (CLASSID, CLASSLEVEL, CLASSTYPE, PARENTECLASSID, DESCRIPTION, PERFIXDES, DOCTABLE, BOXTABLE, ROOLTABLE, PERFIXSTREAM) VALUES (?,?,?,?,?,?,?,?,?,?)";
        List<String> params = new ArrayList<>();
        params.add("CLASSID");
        params.add("CLASSLEVEL");
        params.add("CLASSTYPE");
        params.add("PARENTECLASSID");
        params.add("DESCRIPTION");
        params.add("PERFIXDES");
        params.add("DOCTABLE");
        params.add("BOXTABLE");
        params.add("ROOLTABLE");
        params.add("PERFIXSTREAM");
        List<Map<String, Object>> values = new ArrayList<>();
        Map<String, Object> map = null;
        try {
            map = convertBeanToMap(classTreeInfo);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        values.add(map);
        int ret = DbUtility.execSQLWithTrans(sql, params, values);
        if (ret == 1) {
            jsonReturn.put("result", 1);
            jsonReturn.put("error", "增加档案门类失败");
            return jsonReturn;
        }
        jsonReturn.put("result", 0);
        return jsonReturn;
    }

    /**
     * 删除档案门类
     *
     * @param classId 门类ID
     * @return
     */
    public JSONObject remove(String classId) {
        JSONObject jsonReturn = JSONObject.parseObject("{}");
        String sql = "DELETE FROM CLASSTREE WHERE CLASSID='" + classId + "'";
        int ret = DbUtility.execSQLWithTrans(sql);
        if (ret == 1) {
            jsonReturn.put("result", 1);
            jsonReturn.put("error", "删除档案门类失败");
            return jsonReturn;
        }
        jsonReturn.put("result", 0);
        return jsonReturn;
    }

    private Map<String, Object> convertBeanToMap(Object bean) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        Class type = bean.getClass();
        Map<String, Object> returnMap = new HashMap<>(10);
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if (!"class".equals(propertyName)) {
                Method readMethod = descriptor.getReadMethod();
                Object result = readMethod.invoke(bean, new Object[0]);
                if (result != null) {
                    returnMap.put(propertyName, result);
                } else {
                    returnMap.put(propertyName, "");
                }
            }
        }
        return returnMap;
    }

    /**
     * 根据门类ID和门类类型得到实体表名
     *
     * @param classId
     * @param classType 0：文件级；1：盒级；2：案卷级
     * @return 字符串，如果未找到，返回空字符串
     */
    public String getTableName(String classId, String classType) {
        String sql;
        switch (DbUtility.getDBType()) {
            case DBConsts.DB_SQLSERVER:
            case DBConsts.DB_MYSQL:
                switch (classType) {
                    case "0":
                        sql = "SELECT DOCTABLE AS 'COL' FROM CLASSTREE WHERE CLASSID='" + classId + "'";
                        break;
                    case "1":
                        sql = "SELECT BOXTABLE AS 'COL' FROM CLASSTREE WHERE CLASSID='" + classId + "'";
                        break;
                    case "2":
                        sql = "SELECT ROOLTABLE AS 'COL' FROM CLASSTREE WHERE CLASSID='" + classId + "'";
                        break;
                    default:
                        return "";
                }
                break;
            case DBConsts.DB_ORACLE:
                sql = "SELECT 1";
                break;
            default:
                return null;
        }
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if (list != null && list.size() > 0) {
            return list.get(0).get("COL").toString().trim();
        }
        return "";
    }

    public List<Map<String, Object>> getClassTree(String classId) {
        //String sql = "select * from classtree where classid ='" + classId + "'";
        //SELECT REPLACE(NEWID(), '-', '') as 'newClassId', CLASSID, DESCRIPTION, PERFIXDES, DOCTABLE, BOXTABLE, ROOLTABLE, dbo.GetClassType(DOCTABLE, BOXTABLE, ROOLTABLE) as 'CLASSTYPE' FROM CLASSTREE WHERE CLASSLEVEL='7' AND CLASSTYPE=1

        String sql = "";
        switch (DbUtility.getDBType()) {
            case DBConsts.DB_SQLSERVER:
                sql = "SELECT  CLASSID, DESCRIPTION, PERFIXDES, DOCTABLE, BOXTABLE, ROOLTABLE," +
                        " dbo.GetClassType(DOCTABLE, BOXTABLE, ROOLTABLE) as 'CLASSTYPE' FROM CLASSTREE WHERE classid ='" + classId + "'";
                break;
            case DBConsts.DB_MYSQL: //MYsql text类型字段用 SECURITY <> ''判断为空
                sql = "SELECT  CLASSID, DESCRIPTION, PERFIXDES, DOCTABLE, BOXTABLE, ROOLTABLE," +
                        " GetClassType(DOCTABLE, BOXTABLE, ROOLTABLE) as 'CLASSTYPE' FROM CLASSTREE WHERE classid ='" + classId + "'";
                break;
            case DBConsts.DB_ORACLE:
                sql = "SELECT 1";
                break;
            default:
                return null;
        }


        return DbUtility.execSQL(sql);
    }

    /**
     * 根据用户ID和角色ID，对指定档案库产生一个临时ID，保存到内存数据库中，有效期是24小时
     * 每个用户或角色，每次产生的临时ID都不一样
     *
     * @param userId  用户ID
     * @param roleId  角色ID
     * @param classId 真实的档案库ID
     * @return 临时档案库ID
     */
//    public ClassTreeInfo createMapClassId(String userId, String roleId, String classId) {
//        ClassTreeInfo classInfo = new ClassTreeInfo();
//        List<Map<String, Object>> list;
//        String sql = "SELECT * FROM classtree where classid='" + classId + "'";
//        list = DbUtility.execSQL(sql);
//        Map<String, Object> table = list.get(0);
//        classInfo.setClassId(table.get("CLASSID").toString().trim());
//        if (table.get("BOXTABLE") != null) {
//            classInfo.setBoxTable(table.get("BOXTABLE").toString().trim());
//        }
//        if (table.get("DOCTABLE") != null) {
//            classInfo.setDocTable(table.get("DOCTABLE").toString().trim());
//        }
//        if (table.get("ROOLTABLE") != null) {
//            classInfo.setRoolTable(table.get("ROOLTABLE").toString().trim());
//        }
//        classInfo.setClassType(Integer.valueOf(table.get("CLASSTYPE").toString().trim()));
//        return classInfo;
//    }

    /**
     * 根据当前Sessrion，对指定档案库产生一个临时ID，保存到内存数据库中，有效期是24小时
     * 每个用户或角色，每次产生的临时ID都不一样
     * <p>
     * //     * @param se      当前Session
     * //     * @param classId 真实的档案库ID
     *
     * @return 临时档案库ID
     */
//    public ClassTreeInfo createMapClassId(HttpSession se, String classId) {
//        if (se.getAttribute("userInfo") != null) {
//            UserInfo userInfo = (UserInfo) se.getAttribute("userInfo");
//            String userId = userInfo.getUserId();
//            String roleId = userInfo.getRoleId();
//            return createMapClassId(userId, roleId, classId);
//        }
//        return null;
//    }
    public static void main(String[] args) {
        ClassTree classTree = new ClassTree();
        System.out.println(classTree.getClassTree("USERS000000000000000", "RINFO000000000000000"));
    }
}
