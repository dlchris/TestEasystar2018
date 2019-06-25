package com.tskj.fileManageService.Impl;

import com.tskj.core.db.DbConnection;
import com.tskj.fileManageDao.FileManageDao;
import com.tskj.fileManageDao.Impl.FileManageDaoImpl;
import com.tskj.fileManageService.FileManageService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileManageServiceImpl implements FileManageService {
    private FileManageDao file = new FileManageDaoImpl();

    @Override
    public int hangupFile(String docID, List<Map<String, Object>> fileList) {
        List<String> params = new ArrayList<>();
        params.add("FILEID");
//        params.add("DOCID");
        params.add("CLASSID");
        params.add("FILENAME");
        params.add("FILESIZE");
        params.add("FILETYPE");
        params.add("DESCRIOTIO");
        params.add("PATH");
        params.add("SYSFILENAME");
//        params.add("FILESDATET");

        return file.hangupFile(docID, params, fileList);
    }

    @Override
    public int delFile(String fileID) {
        return file.delFile(fileID);
    }

    @Override
    public int batchDelFile(String[] fileIDs){
        Connection conn = DbConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            List<String> fileIDList = new ArrayList<>(Arrays.asList(fileIDs));
            int ret = file.batchDelFile(conn, fileIDList);
            if (ret == 1){
                conn.rollback();
                return 1;
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public Map<String, Object> getFileName(String fileID) {
        return file.getFileName(fileID);
    }

    @Override
    public List<Map<String, Object>> getFileListByDocID(String docID) {
        return file.getFileListByDocID(docID);
    }

    @Override
    public List<Map<String, Object>> getFileInfoByID(String[] fileIDs) {
        return file.getFileInfoByID(fileIDs);
    }

    @Override
    public List<Map<String, Object>> searchFileByName(String fileName, String docID) {
        return file.searchFileByName(fileName, docID);
    }


}
