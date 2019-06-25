package com.tskj.classtable.biz;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tskj.classtable.statistics.consts.StatisticsType;
import com.tskj.core.system.consts.FileType;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface ClassTableBiz {
    /**
     * 主键字段名
     *
     * @return
     */
    String getKeyFieldName();

    /**
     * 字段列表
     *
     * @return
     */
    List<Map<String, Object>> getFields();

    /**
     * 档案库条目记录列表
     *
     * @param paramsMap
     * @param limit
     * @param page
     * @param sort
     * @param sortType
     * @return
     */
    List<Map<String, Object>> getData(Map<String, Object> paramsMap, int limit, int page, String sort, String sortType);

    /**
     * 条目总数
     *
     * @return
     */
    int getSize(String power);

    /**
     * 门类类型，0：文件级，1：盒级；2：案卷级
     *
     * @return
     */
    int getClassType();

    /**
     * 得到门类ID
     *
     * @return
     */
    String getClassId();

    /**
     * 得到表名
     *
     * @return
     */
    String getTableName();

    /**
     * 判断主键的值的唯一性
     *
     * @param keyValue 主键的值
     * @return 存在：true，不存在：false
     */
    Boolean exists(String keyValue);

    /**
     * 判断多条件下的条目的唯一性
     *
     * @param whereValue 唯一性的条件
     * @return 存在：true，不存在：false
     */
    Boolean exists(Map<String, Object> whereValue);

    /**
     * 保存条目
     *
     * @param value 条目内容
     * @return
     */
    JSONObject save(Map<String, Object> value);

    /**
     * 按主键删除一条记录
     *
     * @param keyValue 主键的值
     * @return 成功，code=0；不成功，code=1；errMsg=错误原因
     */
    JSONObject delete(String keyValue);

    /**
     * 多条件删除记录
     *
     * @param whereValue 删除条件
     * @return 成功，code=0；不成功，code=1；errMsg=错误原因
     */
    JSONObject delete(JSONObject whereValue);

    /**
    * @Description: 事务删除
    * @param
    * @return
    * @author Mao
    * @date 2019/4/22 15:27
    */
    int delete(List<String> keyValues);

    /**
     * 多条件删除
     *
     * @param values
     * @return
     */
    JSONObject delete(JSONArray values);

    //批量删除
    JSONObject mutilDel(JSONArray values);

    //单个删除
    JSONObject singleDel(JSONObject value);

    /**
     * 精确查询
     *
     * @param whereStr
     * @param sortFields
     * @param power 对应权限
     * @return
     */
    Boolean search(String whereStr, String sortFields,String power,String classType);

    /**
     * 导出数据
     *
     * @param fileType 文件类型，xls/xlsx/mdb/accdb
     * @param fileName 文件名
     * @param datas    要导出的数据列表
     * @return 导出结果，code=0：成功，code=其他：失败
     * errMsg：失败信息
     */
    JSONObject export(FileType fileType, String fileName, JSONArray datas);

    /**
     * 获取档案表的字典项字段列表
     *
     * @return
     */
    List<Map<String, Object>> getDictFields();

    /**
     * @param value
     * @return
     * @Author JRX
     * @Description: 用于验证著录条目验证必著项是否填写
     * @create 2019/3/22 14:22
     **/
    boolean isFullValidate(Map<String, Object> value);
    /**
     * @Author JRX
     * @Description: 用于验证编辑条目验证必著项是否填写
     * @create 2019/3/22 14:24
     * @param value
     * @return
    **/
    boolean isFullVldUpdate(Map<String, Object> value);


    //查各部门 年度整理条目数量
    List<Map<String, Object>> findFieldNumByDept(String YEARNO,String CLASSID,String TABLENAME);

}
