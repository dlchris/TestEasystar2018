package com.tskj.fileManageDao.Impl;

import com.tskj.core.db.DbUtility;
import com.tskj.core.system.consts.DBConsts;
import com.tskj.fileManageDao.FileManageDao;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileManageDaoImpl implements FileManageDao {

    /**
    * @Description: 原件挂接
    * @param
    * @return
    * @author Mao
    * @date 2019/3/7 10:14
    */
    @Override
    public int hangupFile(String docID, List<String> params, List<Map<String, Object>> fileList) {
        String sql = "";
        switch (DbUtility.getDBType()) {
            case DBConsts.DB_MYSQL:
                sql = "insert into files (fileid, docid, classid, filename, filesize, filetype, descriotio, path, sysfilenam, filesdatet) " +
                        "values (?,'" + docID + "',?,?,?,?,?,?,?, now())";
                break;
            default:
                sql = "INSERT INTO FILES (FILEID, DOCID, CLASSID, FILENAME, FILESIZE, FILETYPE, DESCRIOTIO, PATH, SYSFILENAM, FILESDATET) " +
                        "VALUES (?,'" + docID + "',?,?,?,?,?,?,?, GETDATE())";
        }
        return DbUtility.execSQLWithTrans(sql, params, fileList);
    }

    /**
    * @Description: 删除原件
    * @param
    * @return
    * @author Mao
    * @date 2019/3/7 10:15
    */
    @Override
    public int delFile(String fileID) {
        String sql = "DELETE FROM FILES WHERE FILEID = '"+fileID+"'";
        return DbUtility.execSQLWithTrans(sql);
    }

    /**
    * @Description: 批量删除原件
    * @param
    * @return
    * @author Mao
    * @date 2019/3/13 14:46
    */
    @Override
    public int batchDelFile(Connection conn, List<String> fileIDs){
        String sql = "DELETE FROM FILES WHERE FILEID = ?";
        return DbUtility.execSQLWithTrans(conn, sql, fileIDs);
    }

    /**
    * @Description: 获取文件名
    * @param
    * @return
    * @author Mao
    * @date 2019/3/7 14:34
    */
    @Override
    public Map<String, Object> getFileName(String fileID){
        String sql = "SELECT DOCID, FILENAME FROM FILES WHERE FILEID = '"+fileID+"'";
        List<Map<String, Object>> list = DbUtility.execSQL(sql);
        if(list != null && !list.isEmpty()){
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getFileListByDocID(String docID) {
        String sql = "SELECT FILEID,DOCID,OFFID,ISDOC,FILENAME,FILESIZE,FILETYPE,DESCRIOTIO,PATH,SEARCH,VER,CRC,SYSFILENAM,WORDVER,SYSTEMVER,DBVER,OTHEREXPLA,FILESDATET FROM FILES WHERE DOCID = '"+docID+"'";
        return DbUtility.execSQL(sql);
    }

    @Override
    public List<Map<String, Object>> getFileInfoByID(String[] fileIDs) {
        String arrStr = Arrays.toString(fileIDs);
        arrStr = arrStr.replace(", ", "', '").replace("[", "'").replace("]", "'");
//        System.out.println(arrStr);
        String sql = "SELECT FILEID, DOCID, FILENAME FROM FILES WHERE FILEID in ("+arrStr+")";
        return DbUtility.execSQL(sql);
    }

    @Override
    public List<Map<String, Object>> searchFileByName(String fileName, String docID) {
        String sql = "SELECT FILEID,DOCID,OFFID,ISDOC,FILENAME,FILESIZE,FILETYPE,DESCRIOTIO,PATH,SEARCH,VER,CRC,SYSFILENAM,WORDVER,SYSTEMVER,DBVER,OTHEREXPLA,FILESDATET FROM FILES WHERE DOCID = '"+docID+"' AND DESCRIOTIO LIKE '%"+fileName+"%'";
        return DbUtility.execSQL(sql);
    }


}
