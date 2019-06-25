package com.tskj.pdf;

import com.sun.istack.internal.NotNull;
import com.tskj.core.system.utility.Tools;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.encryption.*;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * TODO pdf操作类，必须使用save保存
 * 1．Media Box：PDF文件显示或打印的介质的尺寸，和你打印制作PDF时的页面尺寸一样。
 * 2、Crop Box：在AB中显示的页面尺寸，也可以说是显示的成品尺寸。
 * 3、Bleed box：成品的出血框
 * 4、Trim box：印刷的成品尺寸；
 * 5、Art box：其他软件导入此PDF文件时，内容裁切框。
 *
 * @author LeonSu
 */
public class Pdf {

    private final String FONT_SIMSUN_TTC = "simsun.ttc";
    private final String AUTHOR = "天深科技";
    private final String CREATOR = "今易档案";
    private final String PRODUCER = "今易档案";
    private final float PDF_VERSION = 1.4F;

    private File pdfFile = null;
    private PDDocument document = null;

    /**
     * 在插入图片时，是否在此页面增加水印，默认是：true
     */
    private boolean canUseWatermark = true;

    /**
     * 水印文本内容，默认是：今易档案
     */
    private String watermark;
    private TrueTypeCollection ttc;
    private List<String> ttcFontName = new ArrayList<>();
    private List<PDDocument> tmpFileList = new ArrayList<>();

    public Pdf(String pdfFileName, boolean isNew) throws IOException {
        pdfFile = new File(pdfFileName);

        if (pdfFile.exists() && !isNew) {
            document = PDDocument.load(pdfFile, MemoryUsageSetting.setupTempFileOnly());
            if (document.isEncrypted()) {
                document.setAllSecurityToBeRemoved(false);
            }
        } else {
            document = new PDDocument();
        }

        addInfo();
        watermark = "今易档案";
        File ttcFile = new File(Tools.getClassPath("com/tskj/pdf/fonts/").concat(FONT_SIMSUN_TTC));
        if (ttcFile.exists()) {
            if (ttcFile.getName().toLowerCase(Locale.US).endsWith(".ttc")) {
                ttc = new TrueTypeCollection(ttcFile);
                ttc.processAllFonts(new TrueTypeCollection.TrueTypeFontProcessor() {
                    @Override
                    public void process(TrueTypeFont trueTypeFont) throws IOException {
                        ttcFontName.add(trueTypeFont.getName());
                    }
                });
            } else if (ttcFile.getName().toLowerCase(Locale.US).endsWith(".ttf")) {

            }
        }
    }

    private TrueTypeFont getFont(int index) throws IOException {
        if (ttcFontName == null) {
            return null;
        }
        if (index >= 0 && index < ttcFontName.size()) {
            return getFont(ttcFontName.get(index));
        }
        return null;
    }

    private TrueTypeFont getFont(String fontName) throws IOException {
        return ttc.getFontByName(fontName);
    }

    public void setCanUseWatermark(Boolean value) {
        this.canUseWatermark = value;
    }

    public String getWatermark() {
        return watermark;
    }

    public void setWatermark(String watermark) {
        this.watermark = watermark;
    }

    /**
     * 创建一个空白pdf文件
     *
     * @throws IOException
     */
    private void addInfo() throws IOException {
        document.getDocument().setVersion(PDF_VERSION);
        PDDocumentInformation pdd = document.getDocumentInformation();
        pdd.setAuthor(AUTHOR);
        pdd.setCreator(CREATOR);
        pdd.setProducer(PRODUCER);

    }

    /**
     * 保存成文件，并关闭文件流
     *
     * @throws IOException
     */
    public void save() throws IOException {
        if (document != null) {

            document.protect(newProtectionPolicy());

            document.save(pdfFile);
            document.close();
        }
        for (PDDocument doc : tmpFileList) {
            doc.close();
        }
    }

    /**
     * 创建PDF文档的默认权限
     * 默认权限：只有允许播放多媒体，其他权限全禁用
     */
    private ProtectionPolicy newProtectionPolicy() {
        AccessPermission permissions = new AccessPermission();

        //管理页面和书签
        //This will tell if the user can insert/rotate/delete pages.
        permissions.setCanAssembleDocument(false);
        //允许从文档中提取内容
        //This will tell if the user can extract text and images from the PDF document.
        permissions.setCanExtractContent(false);
        //允许播放多媒体
        //This will tell if the user can extract text and images from the PDF document for accessibility purposes.
        permissions.setCanExtractForAccessibility(true);
        //This will tell if the user can fill in interactive forms.
        permissions.setCanFillInForm(false);
        //This will tell if the user can modify contents of the document.
        permissions.setCanModify(false);
        //This will tell if the user can add/modify text annotations, fill in interactive forms fields.
        permissions.setCanModifyAnnotations(false);
        //This will tell if the user can print.
        permissions.setCanPrint(false);
        //This will tell if the user can print the document in a degraded format.
        permissions.setCanPrintDegraded(false);
        //This will tell if the object has been set as read only.
        permissions.setReadOnly();
        ProtectionPolicy protectionPolicy = new StandardProtectionPolicy("", "", permissions);
        protectionPolicy.setEncryptionKeyLength(128);
        return protectionPolicy;
    }

    /**
     * 向pdf文件追加一张图片，将图片名称设为书签，为此页面增加水印
     * 图片格式：jpg,jpeg,bmp,gif,png
     *
     * @param imageFileName
     * @throws IOException
     */
    public void appendImage(String imageFileName) throws IOException {
        File imageFile = new File(imageFileName);
        appendImage(imageFile);
    }

    public void appendImage(File imageFile) throws IOException {
        String labelName = imageFile.getName();
        int dot = labelName.lastIndexOf(46);
        appendImage(imageFile, labelName.substring(0, dot));
    }

    /**
     * 向pdf文件追加一张图片，将图片名称设为书签，为此页面增加水印
     * 图片格式：jpg,jpeg,bmp,gif,png
     *
     * @param imageFile
     * @throws IOException
     */
    public void appendImage(File imageFile, String labelName) throws IOException {

        //创建一个新页面，页面大小是A4
        PDPage page = new PDPage(PDRectangle.A4);
        page.setTrimBox(PDRectangle.A4);

        document.addPage(page);

//        PDImageXObject ximage = CCITTFactory.createFromFile(document, imageFile);
        PDImageXObject ximage = PDImageXObject.createFromFileByExtension(imageFile, document);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, true);

        float width = PDRectangle.A4.getWidth();
        float height = PDRectangle.A4.getHeight();
        float imageWidth = ximage.getImage().getWidth();
        float imageHeight = ximage.getImage().getHeight();

//        page.setMediaBox(new PDRectangle(imageWidth, imageHeight));

        float scale1 = height / imageHeight;
        float scale2 = width / imageWidth;
        float scale = scale1 < scale2 ? scale1 : scale2;
        float newWidth = imageWidth * scale;
        float newHeight = imageHeight * scale;
        float x = width - newWidth;
        float y = height - newHeight;

        //图片默认居中
        contentStream.drawImage(ximage, x / 2, y / 2, newWidth, newHeight);

        if (canUseWatermark) {
            //如果允许，增加水印
            addWatermark(page.getMediaBox(), contentStream);
        }
        contentStream.close();

        //将图片名称保存成为书签
        addBookmark(document.getDocumentCatalog(), page, labelName);


    }


    /**
     * 从pdf文件中删除一张图片
     *
     * @param pageName
     * @throws IOException
     */
    public void removePage(String pageName) throws IOException {
        File imageFile = new File(pageName);
        removePage(imageFile);
    }

    /**
     * 从pdf文件中删除一张图片
     *
     * @param pageFile
     * @throws IOException
     */
    public void removePage(File pageFile) throws IOException {
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDDocumentOutline outlines = catalog.getDocumentOutline();
        if (outlines == null) {
            return;
        }

        Iterator<PDOutlineItem> iterator = outlines.children().iterator();
        PDDocumentOutline newOutlines = new PDDocumentOutline();
        PDOutlineItem newItem;
        PDOutlineItem item;

        while (iterator.hasNext()) {
            item = iterator.next();
            if (!item.getTitle().trim().equalsIgnoreCase(pageFile.getName().trim())) {
                newItem = copyOutlineItem(item);
                newOutlines.addLast(newItem);
            } else {
                COSBase cosBase = item.getAction().getCOSObject().getDictionaryObject(COSName.D);
                PDPageDestination destination = (PDPageDestination) PDDestination.create(cosBase);
                document.removePage(destination.getPage());
            }
        }
        catalog.setDocumentOutline(newOutlines);

    }

    private PDOutlineItem copyOutlineItem(PDOutlineItem item) throws IOException {
        PDOutlineItem newItem = new PDOutlineItem();
        newItem.setAction(item.getAction());
        newItem.setDestination(item.getDestination());
        newItem.setTitle(item.getTitle());
        return newItem;
    }

    /**
     * 创建书签
     *
     * @param catalog
     * @param page
     * @param title
     * @throws IOException
     */
    private void addBookmark(PDDocumentCatalog catalog, PDPage page, String title) throws IOException {
        PDDocumentOutline root = catalog.getDocumentOutline();
        if (root == null) {
            root = new PDDocumentOutline();
        }
        PDOutlineItem child = new PDOutlineItem();
        PDPageDestination dest = new PDPageFitDestination();
        dest.setPage(page);
        PDActionGoTo action = new PDActionGoTo();
        action.setDestination(dest);
        child.setAction(action);
        child.setTitle(title);
        root.addLast(child);
        catalog.setDocumentOutline(root);
    }

    /**
     * 删除书签
     *
     * @param title
     * @throws IOException
     */
    private void removeBookmark(String title) throws IOException {
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDDocumentOutline outlines = catalog.getDocumentOutline();
        if (outlines == null) {
            return;
        }
        PDDocumentOutline newOutlines = new PDDocumentOutline();
        Iterator<PDOutlineItem> iterator = outlines.children().iterator();
        PDOutlineItem newItem;
        PDOutlineItem item;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (!item.getTitle().trim().equalsIgnoreCase(title.trim())) {
                newItem = copyOutlineItem(item);
                newOutlines.addLast(newItem);
            }
        }
        catalog.setDocumentOutline(newOutlines);
    }

    /**
     * 创建水印
     *
     * @param document
     * @param rect
     * @param contentStream
     * @throws IOException
     */
    private void addWatermark(PDDocument document, PDRectangle rect, @NotNull PDPageContentStream contentStream) throws IOException {
        PDFont font = PDType0Font.load(document, getFont(1), true);
//        PDFont font = PDType1Font.TIMES_ROMAN;
        PDExtendedGraphicsState r0 = new PDExtendedGraphicsState();
        // 透明度
        r0.setNonStrokingAlphaConstant(0.2f);
        r0.setAlphaSourceFlag(true);
        contentStream.setGraphicsStateParameters(r0);
        contentStream.setNonStrokingColor(200, 0, 0);
        contentStream.beginText();
//        PDRectangle rect = rect1;//PDRectangle.A4;
//        System.out.println(rect.toString());
//        System.out.println(rect.getHeight() / rect.getWidth());
//        System.out.println(Math.atan2(rect.getHeight(), rect.getWidth()));
//        double d2 = Math.atan2(rect.getHeight(), rect.getWidth());
//        System.out.println(d2);
//        d2 = 180 * d2 / Math.PI;
//        System.out.println(d2);
        float fontSize = Math.round(rect.getWidth() / watermark.length());
        contentStream.setFont(font, fontSize);
        // 获取旋转实例
//        Matrix matrix = ;
//        contentStream.setTextMatrix(Matrix.getRotateInstance(20, 0f, 0.0f));
        contentStream.newLineAtOffset(0, (rect.getHeight() - fontSize) / 2);
        contentStream.showText(watermark);
        contentStream.endText();
    }

    /**
     * 在当前页面中增加水印
     *
     * @param contentStream
     * @throws IOException
     */
    private void addWatermark(PDRectangle rect, @NotNull PDPageContentStream contentStream) throws IOException {
        addWatermark(document, rect, contentStream);
    }

    /**
     * 向pdf文件的所有页面增加水印
     *
     * @throws IOException
     */
    public void addWatermark() throws IOException {
        PDPageContentStream contentStream;
        for (PDPage page : document.getPages()) {
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            addWatermark(page.getMediaBox(), contentStream);
            contentStream.close();
        }
    }

    /**
     * 向pdf文件的指定页面增加水印
     *
     * @param pageIndex
     * @throws IOException
     */
    public void addWatermark(int pageIndex) throws IOException {
        PDPage page = document.getPage(pageIndex);
        addWatermark(document, page);
    }

    /**
     * 向pdf文件的指定页面增加水印
     *
     * @param page
     * @throws IOException
     */
    public void addWatermark(PDPage page) throws IOException {
        addWatermark(document, page);
    }

    private void addWatermark(PDDocument document, PDPage page) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
        addWatermark(document, page.getMediaBox(), contentStream);
        contentStream.close();
    }

    /**
     * 获取当前pdf文件的总页数
     *
     * @return
     * @throws IOException
     */
    public int getPageCount() throws IOException {
        return document.getNumberOfPages();
    }

    /**
     * 获取PDF的总页数
     *
     * @param pdfFile
     * @return
     * @throws IOException
     */
    public static int getPageCount(File pdfFile) throws IOException {
        PDDocument document = PDDocument.load(pdfFile);
        return document.getNumberOfPages();
    }

    /**
     * 获取PDF的总页数
     *
     * @param pdfFileName
     * @return
     * @throws IOException
     */
    public static int getPageCount(String pdfFileName) throws IOException {
        File pdfFile = new File(pdfFileName);
        return getPageCount(pdfFile);
    }

    /**
     * 合并另一个PDF文件
     *
     * @param mergePdfFileName
     * @throws IOException
     */
    public void merge(String mergePdfFileName) throws IOException {
        File mergePdfFile = new File(mergePdfFileName);
        merge(mergePdfFile, "");
    }

    /**
     * 合并另一个PDF文件
     *
     * @param mergePdfFile
     * @throws IOException
     */
    public void merge(File mergePdfFile) throws IOException {
        merge(mergePdfFile, "");
    }

    /**
     * 合并另一个PDF文件
     *
     * @param mergePdfFileName
     * @param mergePassword
     * @throws IOException
     */
    public void merge(String mergePdfFileName, String mergePassword) throws IOException {
        File mergePdfFile = new File(mergePdfFileName);
        merge(mergePdfFile, mergePassword);
    }

    /**
     * 合并另一个PDF文件
     *
     * @param mergePdfFile
     * @param mergePassword
     * @throws InvalidPasswordException
     * @throws IOException
     */
    public void merge(File mergePdfFile, String mergePassword) throws IOException {
        PDDocument mergeDoc = PDDocument.load(mergePdfFile, mergePassword);
        String title = mergePdfFile.getName();
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        for (PDPage page : mergeDoc.getPages()) {
            document.addPage(page);
            addWatermark(document, page);
            addBookmark(catalog, page, title);
        }
        tmpFileList.add(mergeDoc);
    }

    public void close() throws IOException {
        if (document != null) {
            document.close();
        }
    }

    public void delete() {
        pdfFile.delete();
    }


    /**
     * 将指定书签名称的页面从PDF文件中分割成新文件
     *
     * @param newFileName
     * @param pageName
     * @throws IOException
     */
    public void split(String newFileName, String pageName) throws IOException {
        File newFile = new File(newFileName);
        split(newFile, pageName);
    }

    /**
     * 将指定书签名称的页面从PDF文件中分割成新文件
     *
     * @param newFile
     * @param pageName
     * @throws IOException
     */
    public void split(File newFile, String pageName) throws IOException {
        PDDocument doc = new PDDocument();
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDDocumentOutline outlines = catalog.getDocumentOutline();
        if (outlines == null) {
            return;
        }

        Iterator<PDOutlineItem> iterator = outlines.children().iterator();
        PDOutlineItem item;

        while (iterator.hasNext()) {
            item = iterator.next();
            if (item.getTitle().trim().equalsIgnoreCase(pageName.trim())) {
                COSBase cosBase = item.getAction().getCOSObject().getDictionaryObject(COSName.D);
                PDPageDestination destination = (PDPageDestination) PDDestination.create(cosBase);
                doc.addPage(destination.getPage());
            }
        }
        doc.save(newFile);
        doc.close();
    }

    /**
     * @param
     * @return
     * @Description: 按页码拆分PDF文件
     * @author Mao
     * @date 2019/3/24 21:38
     */
    public void splitFileByPage(String newFilePath, int beginPage, int endPage) {
        List<PDDocument> docPages = null;
        try {
            Splitter splitter = new Splitter();
            int countPages = document.getNumberOfPages();
            if (endPage > countPages) {
                endPage = countPages;
            }
            if (beginPage > endPage) {
                beginPage = endPage;
            }
            splitter.setStartPage(beginPage);
            splitter.setEndPage(endPage);
            splitter.setSplitAtPage(endPage - beginPage + 1);      //每几页拆分成一个PDDocument，默认是1
            docPages = splitter.split(document);
            for (PDDocument doc : docPages) {
                writeDocument(doc, newFilePath);
                doc.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param
     * @return
     * @Description: 向PDF文件中写入内容, 会覆盖之前的内容
     * @author Mao
     * @date 2019/3/25 10:57
     */
    private static void writeDocument(PDDocument doc, String fileName) throws IOException {
        FileOutputStream output = null;
        COSWriter writer = null;
        try {
            output = new FileOutputStream(fileName);
            writer = new COSWriter(output);
            writer.write(doc);
        } finally {
            if (output != null) {
                output.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    //删除某一页

    /**
     * @param pdfPath
     * @param flag    0：第一页；1：最后一页 ；else : 要删除的页码
     * @return
     * @Author JRX
     * @Description:
     * @create 2019/6/14 11:01
     **/
    private void cutPdf(String pdfPath, int flag) {
        File file = new File(pdfPath);
        PDDocument document = new PDDocument();
        try {
            document = PDDocument.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int noOfPages = document.getNumberOfPages();
        System.out.println(noOfPages);
        if (flag == 0) {
            //删除第一页
            document.removePage(0);
        } else if (flag == 1) {
            //删除最后一页
            document.removePage(noOfPages - 1);
        } else {
            document.removePage(flag - 1);
        }
        try {
            document.save(pdfPath);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendImage2(File imageFile, String labelName) throws IOException {

        //创建一个新页面，页面大小是A4
        //PDPage page = new PDPage(PDRectangle.A4);
        //page.setTrimBox(PDRectangle.A4);
        //
        //document.addPage(0);
        //替换位置
        PDPage page = document.getPage(0);


//        PDImageXObject ximage = CCITTFactory.createFromFile(document, imageFile);
        PDImageXObject ximage = PDImageXObject.createFromFileByExtension(imageFile, document);
        PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.OVERWRITE, true, true);

        float width = PDRectangle.A4.getWidth();
        float height = PDRectangle.A4.getHeight();
        float imageWidth = ximage.getImage().getWidth();
        float imageHeight = ximage.getImage().getHeight();

//        page.setMediaBox(new PDRectangle(imageWidth, imageHeight));

        float scale1 = height / imageHeight;
        float scale2 = width / imageWidth;
        float scale = scale1 < scale2 ? scale1 : scale2;
        float newWidth = imageWidth * scale;
        float newHeight = imageHeight * scale;
        float x = width - newWidth;
        float y = height - newHeight;

        //图片默认居中
        contentStream.drawImage(ximage, x / 2, y / 2, newWidth, newHeight);

       /* if (canUseWatermark) {
            //如果允许，增加水印
            addWatermark(page.getMediaBox(), contentStream);
        }*/
        contentStream.close();

        //将图片名称保存成为书签
        addBookmark(document.getDocumentCatalog(), page, labelName);


    }

    public static void main(String[] args) throws IOException {
        /*byte[] b = "Sroducts".getBytes();// new byte[]{-102, 50, -77, 85, -75, 97, -28, 90, 111, -64, -114, -73, -29, 65, -116, -108, 36};
        String s = new String(b, "GB2312");
        System.out.println(b[0]);
        System.out.println(s);*/
//        Pdf pdf = null;
//        try {
//            pdf = new Pdf("d:\\aaaa.pdf", false);
////            pdf.setColor(0);
////            pdf.setWatermark("EASYNOW");
////            pdf.addWatermark(pdfFile);
//            pdf.appendImage("d:\\P1030530.jpg");
////            pdf.appendImage( "d:\\000000001-0001·001_001_003.JPG");
////            pdf.removeImage( "d:\\P1030530.jpg");
////            df.removePage("d:\\a1.pdf");
//            pdf.merge("d:\\a1.pdf");
//            pdf.split("d:\\a2.pdf", "P1030530.jpg");
//            pdf.save();
////            pdf.merge("D:\\a1.pdf");
////            Pdf.merge();
//        } catch (IOException e) {
//            e.printStackTrace();
//            pdf.close();
//            pdf.delete();
//        }


        /*Pdf pdf = new Pdf("d:\\22.pdf", false);
        pdf.merge("D:\\2.pdf");
       // pdf.appendImage("F:\\Download\\files\\1405563549408.jpg");
        pdf.save();*/
        //Pdf pdf = new Pdf("F:\\testPdf\\2222.pdf", false);
        Pdf pdf = new Pdf("F:\\upload\\f51fedb6ef664e9fb2738c868ed47c1b", false);
        //pdf.merge("F:\\testPdf\\2.pdf");
        File file = new File("F:\\testPdf\\1.png");
        //pdf.appendImage(file, "替换内容页");
        pdf.appendImage2(file, "替换内容页");
        pdf.save();

        /*long start = System.currentTimeMillis();
        Pdf pdf = new Pdf("f:/mergeFile/be800ab0a1914e0e90ef8586e7e6c7eb", false);
//        pdf.splitFileByPage("f:/mergeFile/splitFile/test.pdf", 1, 3);

        File file = new File("f:/mergeFile/new.pdf");
        pdf.split(file, "7ce506eada9d477396ec461599b288f8.pdf");
        long needTime = System.currentTimeMillis() - start;
        Pdf pdf1 = new Pdf("f:/mergeFile/new.pdf", false);
        System.out.println(pdf1.getPageCount() + "页");
        System.out.println(needTime + "ms");     //1451ms   49页*/

        //Pdf pdf = new Pdf("f:/mergeFile/new.pdf", false);
        //pdf.appendImage("f:/upload/26ba7e2c85e249d492de3556e8fca851");
        /*Pdf pdf = new Pdf("F:\\testPdf\\aaaa.pdf", false);
        pdf.appendImage("F:\\testPdf\\1.png");*/

    }
}
