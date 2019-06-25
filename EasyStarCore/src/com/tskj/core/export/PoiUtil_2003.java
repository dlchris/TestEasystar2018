package com.tskj.core.export;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author qjwyss
 * @date 2018/9/18
 * @description POI导出工具类
 */
public class PoiUtil_2003 {

    /**
     * 初始化EXCEL(sheet个数和标题)
     *
     * @param totalRowCount 总记录数
     * @param titles        标题集合
     * @return XSSFWorkbook对象
     */
    public static HSSFWorkbook initExcel(Integer totalRowCount, Object[] titles) {

        // 在内存当中保持 100 行 , 超过的数据放到硬盘中在内存当中保持 100 行 , 超过的数据放到硬盘中
        HSSFWorkbook wb = new HSSFWorkbook();

        Integer sheetCount = ((totalRowCount % ExcelConstant.PER_SHEET_ROW_COUNT_2003 == 0) ?
                (totalRowCount / ExcelConstant.PER_SHEET_ROW_COUNT_2003) : (totalRowCount / ExcelConstant.PER_SHEET_ROW_COUNT_2003 + 1));

        // 根据总记录数创建sheet并分配标题
        for (int i = 0; i < sheetCount; i++) {
            String sheetName = "datatable";
            if (i > 0) {
                sheetName += i;
            }
            HSSFSheet sheet = wb.createSheet(sheetName);
            HSSFRow headRow = sheet.createRow(0);

            for (int j = 0; j < titles.length; j++) {
                HSSFCell headRowCell = headRow.createCell(j);
                headRowCell.setCellValue(titles[j].toString().trim());
            }
        }

        return wb;
    }


    /**
     * 下载EXCEL到本地指定的文件夹
     *
     * @param wb         EXCEL对象SXSSFWorkbook
     * @param exportPath 导出路径
     */
    public static void downLoadExcelToLocalPath(HSSFWorkbook wb, String exportPath) {
        FileOutputStream fops = null;
        try {
            fops = new FileOutputStream(exportPath);
            wb.write(fops);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != wb) {
                try {
                    wb.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != fops) {
                try {
                    fops.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 下载EXCEL到浏览器
     *
     * @param wb       EXCEL对象XSSFWorkbook
     * @param response
     * @param fileName 文件名称
     * @throws IOException
     */
    public static void downLoadExcelToWebsite(HSSFWorkbook wb, HttpServletResponse response, String fileName) throws IOException {

        //设置下载的文件名
        //response.setHeader("Content-disposition", "attachment;filename=" + new String((fileName + ".xls").getBytes("utf-8"), "ISO8859-1"));
        //response.setHeader("Content-disposition", "attachment;filename="
        //        + new String(fileName.getBytes("gb2312"), "ISO8859-1") + ".xls");

        response.setContentType("application/x-msdownload");//三大主流浏览器（IE、Firefox、Google ）
        response.setHeader("Content-Disposition", "attachment;filename=" + new String((fileName + ".xls").getBytes("utf-8"), "ISO8859-1"));
        //response.setHeader("Content-Disposition", "attachment;filename="
        //        + new String((fileName + ".xls").getBytes(), "iso-8859-1"));
        Date date = new Date();
        fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
        response.setHeader("FileName", fileName + ".xls");
        response.setHeader("Access-Control-Expose-Headers", "FileName");
        //ServletOutputStream outputStream = response.getOutputStream();
        //wb.write(outputStream);
        //outputStream.close();



        /*response.setContentType("octets/stream");
		String fileNameString = "attachment;filename=" + fileName;
		//设置文件名称
		response.addHeader("Content-Disposition", fileNameString);
		//处理表格名称，转码防止乱码
		response.addHeader("Content-Disposition", "attachment;filename="+new String( excelName.getBytes("gb2312"), "ISO8859-1" )+".xls");  */

        //OutputStream outputStream = null;
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            wb.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != wb) {
                try {
                    wb.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 导出Excel到本地指定路径
     *
     * @param totalRowCount           总记录数
     * @param titles                  标题
     * @param exportPath              导出路径
     * @param writeExcelDataDelegated 向EXCEL写数据/处理格式的委托类 自行实现
     * @throws Exception
     */
    public static final void exportExcelToLocalPath(Integer totalRowCount, String[] titles, String exportPath, WriteExcelDataDelegated_2003 writeExcelDataDelegated) throws Exception {

//        logger.info("开始导出：" + DateUtil.formatDate(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS));

        // 初始化EXCEL
        HSSFWorkbook wb = PoiUtil_2003.initExcel(totalRowCount, titles);

        writeData(wb, writeExcelDataDelegated);

        // 下载EXCEL
        PoiUtil_2003.downLoadExcelToLocalPath(wb, exportPath);

//        logger.info("导出完成：" + DateUtil.formatDate(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS));
    }


    /**
     * 导出Excel到浏览器
     *
     * @param response
     * @param totalRowCount           总记录数
     * @param fileName                文件名称
     * @param titles                  标题
     * @param writeExcelDataDelegated 向EXCEL写数据/处理格式的委托类 自行实现
     * @throws Exception
     */
    public static final void exportExcelToWebsite(HttpServletResponse response, Integer totalRowCount, String fileName, Object[] titles, WriteExcelDataDelegated_2003 writeExcelDataDelegated) throws Exception {

//        logger.info("开始导出：" + DateUtil.formatDate(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS));

        // 初始化EXCEL
        HSSFWorkbook wb = PoiUtil_2003.initExcel(totalRowCount, titles);

        writeData(wb, writeExcelDataDelegated);

        // 下载EXCEL
        PoiUtil_2003.downLoadExcelToWebsite(wb, response, fileName);

//        logger.info("导出完成：" + DateUtil.formatDate(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS));
    }

    private static final void writeData(HSSFWorkbook wb, WriteExcelDataDelegated_2003 writeExcelDataDelegated) throws Exception {
        // 调用委托类分批写数据
        int sheetCount = wb.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            HSSFSheet eachSheet = wb.getSheetAt(i);

            for (int j = 1; j <= ExcelConstant.PER_SHEET_WRITE_COUNT_2003; j++) {

                int currentPage = i * ExcelConstant.PER_SHEET_WRITE_COUNT_2003 + j;
                int pageSize = ExcelConstant.PER_WRITE_ROW_COUNT_2003;
                int startRowCount = (j - 1) * ExcelConstant.PER_WRITE_ROW_COUNT_2003 + 1;
                int endRowCount = startRowCount + pageSize - 1;

                writeExcelDataDelegated.writeExcelData(eachSheet, startRowCount, endRowCount, currentPage, pageSize);

            }
        }
    }
}
