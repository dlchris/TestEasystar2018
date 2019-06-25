package com.easystar.system.files;

import com.tskj.core.db.DbUtility;

import java.util.List;
import java.util.Map;

public class FileManager {
    public List<Map<String, Object>> getFileListByDocId(String docId) {
//        String sql = "SELECT * FROM FILES WHERE DOCID='" + docId + "' ORDER BY DESCRIOTIO";
        String sql = "SELECT FILEID,DOCID,OFFID,ISDOC,FILENAME,FILESIZE,FILETYPE,DESCRIOTIO,PATH,SEARCH,VER,CRC,SYSFILENAM,WORDVER,SYSTEMVER,DBVER,OTHEREXPLA,FILESDATET FROM FILES WHERE DOCID = '"+docId+"' ORDER BY DESCRIOTIO";
//        System.out.println(sql);
        return DbUtility.execSQL(sql);
    }

    public List<Map<String, Object>> getFileListByClassId(String classId) {
        String sql = "SELECT * FROM FILES WHERE CLASSID='" + classId + "' ORDER BY DESCRIOTIO";
        return DbUtility.execSQL(sql);
    }

}
