package com.tskj.pdf;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;

import java.io.*;

/**
 * word文件转pdf的工具类<br>
 * 支持2003和2007格式
 * @author LeonSu
 */
public class Word2Pdf extends ConvertUtil {

    @Override
    public boolean convert(String docFileName, String pdfFileName) {
        if (!getLicense()) {
            return false;
        }
        FileOutputStream fileOS = null;
        try {
            // 原始doc路径
            Document wb = new Document (docFileName);
            // 输出路径
            File pdfFile = new File(pdfFileName);
            fileOS = new FileOutputStream(pdfFile);

            wb.save(fileOS, SaveFormat.PDF);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if(fileOS!=null){
                    fileOS.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Word2Pdf word2Pdf = new Word2Pdf();
        word2Pdf.convert("F:/upload/8bf2904e8a46465db1a8a44da351872a.doc", "F:/upload/8bf2904e8a46465db1a8a44da351872a.pdf");
    }

}
