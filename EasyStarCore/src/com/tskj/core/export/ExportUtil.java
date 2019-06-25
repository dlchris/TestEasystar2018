package com.tskj.core.export;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.NotNull;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 导出数据到文件的工具类
 */
public final class ExportUtil {
    public static void exportToXls(String fileName, JSONArray jsonArray, HttpServletResponse response) throws Exception {
        // 用来反馈函数调用结果
        JSONObject result = new JSONObject();


        result.put("result", "successed");

        // 总记录数
        Integer totalRowCount = jsonArray.size();

        Set<String> keys = ((JSONObject) jsonArray.get(0)).keySet();
        List<String> titles = new ArrayList<>(keys.size());
        for (Object key : keys) {
            titles.add(key.toString());
        }
        // 开始导入
        PoiUtil_2003.exportExcelToWebsite(response, totalRowCount, fileName,titles.toArray(), new WriteExcelDataDelegated_2003() {
            @Override
            public void writeExcelData(HSSFSheet eachSheet, Integer startRowCount, Integer endRowCount, Integer currentPage, Integer pageSize) throws Exception {

                for (int i = startRowCount; i <= endRowCount; i++) {
                    HSSFRow eachDataRow = eachSheet.createRow(i);
                    if ((i - startRowCount) < jsonArray.size()) {
                        JSONObject eachUserVO = (JSONObject) jsonArray.get(i - startRowCount);
                        // ---------   这一块变量照着抄就行  强迫症 后期也封装起来     -----------------------
                        for (int j = 0; j < titles.size(); j++) {
                            eachDataRow.createCell(j).setCellValue(eachUserVO.getString(titles.get(j)).trim());
                        }
                    }
                }
            }
        });
//        return result;
    }

    public static void exportToXlsx(String fileName, @NotNull JSONArray jsonArray, HttpServletResponse response) throws Exception {
        // 用来反馈函数调用结果
        JSONObject result = new JSONObject();


        result.put("result", "successed");

        // 总记录数
        Integer totalRowCount = jsonArray.size();

        Set<String> keys = ((JSONObject) jsonArray.get(0)).keySet();
        List<String> titles = new ArrayList<>(keys.size());
        for (Object key : keys) {
            titles.add(key.toString());
        }
        // 开始导入
        PoiUtil.exportExcelToWebsite(response, totalRowCount, fileName,titles.toArray(), new WriteExcelDataDelegated() {
            @Override
            public void writeExcelData(SXSSFSheet eachSheet, Integer startRowCount, Integer endRowCount, Integer currentPage, Integer pageSize) throws Exception {

                for (int i = startRowCount; i <= endRowCount; i++) {
                    SXSSFRow eachDataRow = ((SXSSFSheet) eachSheet).createRow(i);
                    if ((i - startRowCount) < jsonArray.size()) {
                        JSONObject eachUserVO = (JSONObject) jsonArray.get(i - startRowCount);
                        // ---------   这一块变量照着抄就行  强迫症 后期也封装起来     -----------------------
                        for (int j = 0; j < titles.size(); j++) {
                            eachDataRow.createCell(j).setCellValue(eachUserVO.getString(titles.get(j)).trim());
                        }
                    }
                }
            }
        });

    }

    /**
    * @Description: 导出为.mdb文件
    * @param
    * @return
    * @author Mao
    * @date 2019/2/20 12:30
    */
    public static void exportToMdb(String fileName, JSONArray jsonArray, HttpServletResponse response) throws IOException {
//        String blankPath = new ExportUtil().getClass().getClassLoader().getResource("").getPath();
        String sourcePath = AccessUtil.class.getClassLoader().getResource("").getPath().replace("WEB-INF/classes/", "files/").replace("%20", " ").substring(1);
//        String blankFilePath = sourcePath + "blank.mdb";    //空文件路径
        String downloadFileName = fileName + ".mdb";
        String downloadFilePath = sourcePath + downloadFileName;      //复制得到的文件路径
        //向文件中插入内容
        AccessUtil.getAccessFile(downloadFilePath, jsonArray);
        //下载文件,然后删除文件
        AccessUtil.downLoadFile(downloadFilePath, response);

    }

    /**
    * @Description: 导出为.accdb文件
    * @param
    * @return
    * @author Mao
    * @date 2019/2/20 12:31
    */
    public static void exportToAccdb(String fileName, JSONArray jsonArray, HttpServletResponse response) {
        String sourcePath = AccessUtil.class.getClassLoader().getResource("").getPath().replace("WEB-INF/classes/", "files/").replace("%20", " ").substring(1);
//        String blankFilePath = sourcePath + "blank.accdb";    //空文件路径
        String downloadFileName = fileName + ".accdb";
        String downloadFilePath = sourcePath + downloadFileName;      //复制得到的文件路径
        //向文件中插入内容
        AccessUtil.getAccessFile(downloadFilePath, jsonArray);
        //下载文件,然后删除文件
        AccessUtil.downLoadFile(downloadFilePath, response);
    }

}
