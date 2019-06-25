package com.tskj.pdf;

import com.aspose.words.License;
import com.tskj.core.system.utility.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public abstract class ConvertUtil {
    /**
     * 获取license
     *
     * @return
     */
    protected boolean getLicense() {
        boolean result = false;
        try {
            InputStream is = new FileInputStream(new File(Tools.getClassPath("/").concat("/license.xml")));
            License aposeLic = new License();
            aposeLic.setLicense(is);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public abstract boolean convert(String fileName, String pdfFileName);
}
