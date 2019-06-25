package com.tskj.user.userRightServiceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.tskj.core.system.utility.Tools;
import com.tskj.user.userRightDAO.ContentPowerDao;
import com.tskj.user.userRightDAOImpl.ContentPowerDaoImpl;
import com.tskj.user.userRightService.ContentPowerService;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-25 11:12
 **/
public class ContentPowerServiceImpl implements ContentPowerService {
    ContentPowerDao cpd = new ContentPowerDaoImpl();

    @Override
    public String ContentAuthority(String USERID, String MODULEID, String ROLEID,String classType) {

        //获得权限信息
        List<Map<String, Object>> list = cpd.ContentAuthority(USERID, MODULEID, ROLEID);
        TreeSet<String> set = new TreeSet<>();
        TreeSet<String> set2 = new TreeSet<>();
        //解析内容 "秘密+公开+绝密+国内+内部+机密+测试"
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {

                String SECURITY = Tools.toString(map.get("SECURITY"));
                //String SECURITY = toString(map.get("SECURITY"));
                //加入集合去重复
                if (!"".equals(SECURITY.trim())) {
                    String[] split = SECURITY.split("\\+");
                    set.addAll(Arrays.asList(split));
                }
                String DEPARTMENT = Tools.toString(map.get("SECURITY2"));
                //String DEPARTMENT = toString(map.get("SECURITY2"));
                if (!"".equals(DEPARTMENT.trim())) {
                    String[] split2 = DEPARTMENT.split("\\+");
                    set2.addAll(Arrays.asList(split2));
                }
            }
        }
        //获取最终数据
        String[] strings = set.toArray(new String[set.size()]);
        String[] strings2 = set2.toArray(new String[set2.size()]);
        //(SECURITY = '' OR SECURITY IS NULL) 	AND  (DEPARTMENT IS NULL OR DEPARTMENT = '')
        StringBuilder securitySql = new StringBuilder(" SECURITY = '' OR SECURITY IS NULL ");
        for (String s : strings) {
            securitySql.append(" OR SECURITY= '" + s + "'");
        }
        StringBuilder departmentSql = new StringBuilder(" DEPARTMENT IS NULL OR DEPARTMENT = '' ");
        for (String s : strings2) {
            departmentSql.append(" OR DEPARTMENT = '" + s + "'");
        }
        //System.err.println(securitySql.toString());
        //System.err.println(departmentSql.toString());

        StringBuilder querySql = new StringBuilder();
        if (classType.equalsIgnoreCase("BOXID")) {
            querySql.append("  ( " + departmentSql + " )  ");
        }else{
            querySql.append("  ( " + securitySql + " ) AND ( " + departmentSql + " )  ");
        }

        //System.err.println(querySql.toString());
        return querySql.toString();
    }

    @Override
    public String BoxContentAuthority(String USERID, String MODULEID, String ROLEID) {

        return null;
    }

    @Override
    public JSONObject FindPowerDict(String USERID, String MODULEID, String ROLEID, JSONObject jsonObject) {

        //获得权限信息
        List<Map<String, Object>> list = cpd.ContentAuthority(USERID, MODULEID, ROLEID);
        TreeSet<String> set = new TreeSet<>();
        TreeSet<String> set2 = new TreeSet<>();
        //解析内容 "秘密+公开+绝密+国内+内部+机密+测试"
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {

                String SECURITY = Tools.toString(map.get("SECURITY"));
                //加入集合去重复
                if (!"".equals(SECURITY.trim())) {
                    String[] split = SECURITY.split("\\+");
                    set.addAll(Arrays.asList(split));
                }
                String DEPARTMENT = Tools.toString(map.get("SECURITY2"));
                if (!"".equals(DEPARTMENT.trim())) {
                    String[] split2 = DEPARTMENT.split("\\+");
                    set2.addAll(Arrays.asList(split2));
                }
            }
        }
        //获取最终数据
        String[] SECURITY = set.toArray(new String[set.size()]);
        String[] DEPARTMENT  = set2.toArray(new String[set2.size()]);
        //存在单位字典项
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        JSONArray newList = new JSONArray();
        JSONArray newList2 = new JSONArray();
        if(jsonObject.containsKey("DEPARTMENT")){
            JSONArray department = jsonObject.getJSONArray("DEPARTMENT");
            for (Object obj : department) {
                map = (JSONObject) obj;
                //存在在权限数组中保留
                if (ArrayUtils.contains(DEPARTMENT, map.get("DVALUE"))) {
                    newList.add(map);
                }
            }
            //JSONArray value = jsonGet.getJSONArray("data");
        }
        if(jsonObject.containsKey("SECURITY")){//存在归档机构字典项
            JSONArray security = jsonObject.getJSONArray("SECURITY");
            for (Object obj : security) {
                map2 = (JSONObject) obj;
                //存在在权限数组中保留
                //System.err.println(map2.get("DVALUE"));
                if (ArrayUtils.contains(SECURITY, map2.get("DVALUE"))) {
                    newList2.add(map2);
                }
            }
        }
        jsonObject.put("DEPARTMENT", newList);
        jsonObject.put("SECURITY", newList2);
        //System.err.println(newList);
        //System.err.println(newList2);
        //System.err.println(jsonObject);


        return jsonObject;
    }

    public static void main(String[] args) {
        //ContentPowerService c = new ContentPowerServiceImpl();
        //c.ContentAuthority("USERS000000000000000", "7114C8BDC95041119DFE7226F525CC94", "RINFO000000000000000");
        //c.ContentAuthority("USERS000000000000000", "54CA237FF2FC455C87F88286B94D1FFF", "RINFO000000000000000",);
    }

    /**
     * 字符串工具
     *
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        return (obj == null ? "" : obj.toString());
    }

}
