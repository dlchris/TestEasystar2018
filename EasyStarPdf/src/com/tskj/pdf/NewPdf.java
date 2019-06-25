package com.tskj.pdf;

import com.tskj.core.system.utility.Tools;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.Encoding;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;

public class NewPdf {
    public void createPDF(String fileName) throws IOException {
        PDFont font = null;
        PDDocument doc = null;
        PDPage page = null;

        doc = new PDDocument();
//            page = new PDPage();
//            doc.addPage(page);

//            File ttcFile = new File(Tools.getClassPath("com/tskj/pdf/fonts/").concat("SIMSUN.TTC"));
//            TrueTypeCollection ttc = null;
//            ttc = new TrueTypeCollection(ttcFile);
//            TrueTypeFont ttfFont = ttc.getFontByName("SimSun");
//            font =  PDType0Font.load(doc, ttfFont, true);

//            PDPageContentStream content = new PDPageContentStream(doc, page);
//            content.beginText();
//            content.setFont(font, 12);
//            content.newLineAtOffset(0, 0);
//            content.showText("今易档案，EasyStar!");
//
//            content.endText();
//            content.close();
        doc.getDocument().setVersion(1.7F);
        PDDocumentInformation pdd = doc.getDocumentInformation();
        pdd.setAuthor("今易档案");
        pdd.setCreator("天深科技");
        pdd.setProducer("天深科技");
        doc.save(fileName);
        doc.close();
    }

    public void addImage(String pdfFileName, String imageFileName) throws IOException {
        File pdfFile = new File(pdfFileName);
        PDDocument doc = PDDocument.load(pdfFile);
        int pageCount = doc.getNumberOfPages();
        PDPage page = new PDPage();
        doc.addPage(page);
//        PDXObjectImage ximage = null;
        PDImageXObject ximage = new PDImageXObject(doc);
        System.out.println(doc.getNumberOfPages());
    }
    public static void main(String[] args) {
        NewPdf pdf = new NewPdf();
        try {
            pdf.createPDF("F:\\testPdf\\aaaa.pdf");
            pdf.addImage("F:\\testPdf\\aaaa.pdf", "F:\\testPdf\\1.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
