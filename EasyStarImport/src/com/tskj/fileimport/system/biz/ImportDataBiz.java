package com.tskj.fileimport.system.biz;

import com.alibaba.fastjson.JSONObject;
import com.tskj.fileimport.system.consts.DataTypeConsts;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;
import java.util.Map;

public interface ImportDataBiz {

    /**
     * 获取所有的列名
     *
     * @return ArrayList<String>
     */
    List<String> getColumns();

    /**
     * 获取数据总数
     *
     * @return int
     */
    int getSize();

    /**
     * @return
     * @Author JRX
     * @Description: 获取空行之前的数据总行数  通过主键判断当前行所有
     * @create 2018/11/15 11:06
     * @Param fieldName 主键的字段名 String fieldName
     **/
    int getSizeCount();

    /***
     * @Author JRX
     * @Description: 判断空行
     * @create 2018/11/15 11:23
     * @Param [row]
     * @return
     **/
    boolean isRowEmpty(Row row);

    /**
     * 获取单条记录
     *
     * @param index
     * @return
     */
    Map<String, Object> getData(int index);

    /**
     * @param
     * @return
     * @Author JRX
     * @Description: 初始化一个对象
     * @create 2019/3/14 15:42
     **/
    //void fieldsInIt();

    /**
     * 获取所有记录
     *
     * @return
     */
    List<Map<String, Object>> getDataAll();

    /**
     * 保存记录
     *
     * @param index
     * @return
     */
    Object save(int index);

    boolean deleteFile();

    /**
     * 校验主键的值是否唯一
     *
     * @param fieldName 主键的字段名
     * @return 如果唯一，返回：true，否则返回：false
     */
    boolean verfiy(String fieldName);

    /**
     * 校验字段的类型与长度是否一致
     *
     * @param sourceFieldName 来源字段
     * @param targetFieldName 目标字段
     * @return
     */
    JSONObject verifyDataType(String sourceFieldName, DataTypeConsts fieldType, int fieldSize, String targetFieldName);
}
