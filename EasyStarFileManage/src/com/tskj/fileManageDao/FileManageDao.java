package com.tskj.fileManageDao;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface FileManageDao {

    /**
    * @Description: 挂接原件
    * @param
    * @return
    * @author Mao
    * @date 2019/3/7 10:13
    */
    int hangupFile(String docID, List<String> params, List<Map<String, Object>> fileList);

    /**
    * @Description: 取消原件挂接，删除原件
    * @param
    * @return
    * @author Mao
    * @date 2019/3/7 10:13
    */
    int delFile(String fileID);

    /**
    * @Description: 批量删除原件
    * @param
    * @return
    * @author Mao
    * @date 2019/3/13 14:43
    */
    int batchDelFile(Connection conn, List<String> fileIDs);

    /**
    * @Description: 根据文件ID获取文件名
    * @param
    * @return
    * @author Mao
    * @date 2019/3/7 14:35
    */
    Map<String, Object> getFileName(String fileID);

    /**
    * @Description: 根据条目ID获取条目下挂接原件列表
    * @param
    * @return
    * @author Mao
    * @date 2019/3/12 11:16
    */
    List<Map<String, Object>> getFileListByDocID(String docID);

    /**
    * @Description: 根据原件ID获取原件信息
    * @param
    * @return
    * @author Mao
    * @date 2019/3/13 14:55
    */
    List<Map<String, Object>> getFileInfoByID(String[] fileIDs);


    /**
     * @Description: 根据原件名搜索原件
     * @param
     * @return
     * @author Mao
     * @date 2019/3/19 9:37
     */
    List<Map<String, Object>> searchFileByName(String fileName, String docID);

}
