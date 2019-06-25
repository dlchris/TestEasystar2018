package com.tskj.pdf;

import com.aspose.cells.License;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;

import java.io.*;

/**
 * Excel文件转pdf的工具类<br>
 * 支持2003和2007格式
 * @author LeonSu
 */
public class Excel2Pdf extends ConvertUtil {

    @Override
    public boolean convert(String fileName, String pdfFileName) {
        if (!getLicense()) {
            return false;
        }
        try {
            // 原始excel路径
            Workbook wb = new Workbook(fileName);
            // 输出路径
            File pdfFile = new File(pdfFileName);
            FileOutputStream fileOS = new FileOutputStream(pdfFile);
            wb.save(fileOS, SaveFormat.PDF);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        Excel2Pdf excel2Pdf = new Excel2Pdf();      //License导包不同
        excel2Pdf.convert("D:\\文件\\测试文件\\abc.xls", "D:\\文件\\测试文件\\abc.pdf");
    }

}
