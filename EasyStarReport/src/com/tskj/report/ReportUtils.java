package com.tskj.report;

import com.tskj.core.system.utility.Tools;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRAbstractLRUVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * 使用jasperReport做报表时的工具支持类.有两个用途,生成jasperPrint对象,和设置导出时的session
 */
public class ReportUtils {
    //    private static final Logger LOGGER = LoggerFactory.getLogger(JasperreportUtils.class);

    public ReportUtils() {
        super();
        virtualizer = new JRGzipVirtualizer(2);
    }

    /**
     * datasource与parameters填充报表
     *
     * @param jasperPath
     * @param dataSource
     * @param parameters
     * @return
     * @throws JRException
     */
    public JasperPrint getJasperPrint(String jasperPath, Map<String, Object> parameters, JRDataSource dataSource)
            throws JRException {

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, parameters, dataSource);
        return jasperPrint;
    }

    /**
     * connection与parameters填充报表
     *
     * @param jasperPath
     * @param conn
     * @param parameters
     * @return
     * @throws JRException
     */
    public JasperPrint getJasperPrint(String jasperPath, Map<String, Object> parameters, Connection conn)
            throws JRException {

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, parameters, conn);

        return jasperPrint;
    }

    /**
     * 传入list获取jasperPrint
     *
     * @param jasperPath
     * @param parameters
     * @param list
     * @return
     * @throws JRException
     */
    public JasperPrint getJasperPrintWithBeanList(String jasperPath, Map<String, Object> parameters, List<?> list)
            throws JRException {
        JRDataSource dataSource = null;
        if (null != list && list.size() > 0) {
            dataSource = new JRBeanCollectionDataSource(list);
        } else {
            dataSource = new JREmptyDataSource();
        }
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, parameters, dataSource);
        return jasperPrint;
    }

    public JasperPrint getJasperPrintWithBeanList(String jasperPath, Map<String, Object> parameters, JRDataSource dataSource)
            throws JRException {
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, parameters, dataSource);
        return jasperPrint;
    }

    /**
     * 获得相应类型的Content type
     *
     * @param docType
     * @return
     */
    public String getContentType(DocType docType) {
        String contentType = "text/html";
        switch (docType) {
            case XLS:
                contentType = "application/vnd.ms-excel";
                break;
            case XLSX:
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                break;
            case XML:
                contentType = "text/xml";
                break;
//            case RTF:
//                contentType = "application/rtf";
//                break;
            case CSV:
                contentType = "text/plain";
                break;
//            case DOC:
//                contentType = "application/msword";
//                break;
            case DOCX:
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                break;
            case PDF:
            default:
                contentType = "application/pdf";
                break;
        }
        return contentType;
    }

    /**
     * jrxml文件 编译为 jasper文件
     *
     * @param jrxmlPath
     * @param jasperPath
     * @throws JRException
     */
    public void jrxmlToJsper(String jrxmlPath, String jasperPath) throws JRException {

        JasperCompileManager.compileReportToFile(jrxmlPath, jasperPath);

    }

    /**
     * 将pdf输出到浏览器上
     *
     * @param jasperPath
     * @param parameters
     * @param downloadName
     * @param dataSource
     * @throws IOException
     * @throws JRException
     */
    public void exportPdf(OutputStream outputStream, String jasperPath, Map<String, Object> parameters, String downloadName,
                          JRDataSource dataSource) throws IOException, JRException {
        FileInputStream isRef = new FileInputStream(new File(jasperPath));
        ;
        // 放开下载
        // response.setHeader("Content-Disposition", "attachment;filename=\"" +
        // downloadName + ".pdf\"");
        JasperRunManager.runReportToPdfStream(isRef, outputStream, parameters, dataSource);
    }

    /**
     * 生成html文件
     *
     * @param response
     * @param list
     * @param jasperPath
     * @param fileName
     * @param parameters
     * @return
     */
    public String createHtml(HttpServletResponse response, List<?> list, String jasperPath, String fileName,
                             Map<String, Object> parameters, String folder) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String dpath = sdf.format(new Date());
        String path = Tools.getClassPath("/reportHtml") + folder + "/" + dpath;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String htmlFilePath = path + "/" + fileName;
        try {

            JRDataSource dataSource = null;
            if (null != list && list.size() > 0) {
                dataSource = new JRBeanCollectionDataSource(list);
            } else {
                dataSource = new JREmptyDataSource();
            }

            JasperPrint jasperPrint = this.getJasperPrint(jasperPath, parameters, dataSource);

            JasperExportManager.exportReportToHtmlFile(jasperPrint, htmlFilePath);

        } catch (Exception ex) {
//            LOGGER.error("生成html文件错误" + ex.getMessage(), ex);
        }
        return folder + "/" + dpath + "/" + fileName;
    }

    /**
     * 传入类型，获取输出器
     *
     * @param docType
     * @return
     */
    @SuppressWarnings("deprecation")
    public JRAbstractExporter getJRExporter(DocType docType, Object out) {
        JRAbstractExporter exporter = null;
        SimpleReportExportConfiguration configuration;
        ExporterOutput outPut;
        switch (docType) {
            case PDF:
                exporter = new JRPdfExporter();
                outPut = new SimpleOutputStreamExporterOutput((OutputStream) out);
                break;
            case HTML:
                exporter = new HtmlExporter();
                outPut = new SimpleHtmlExporterOutput((PrintWriter) out);
                break;
            case XLS:
                exporter = new JRXlsExporter();
                outPut = new SimpleOutputStreamExporterOutput((OutputStream) out);
                configuration = new SimpleXlsReportConfiguration();

                // 删除记录最下面的空行
                ((SimpleXlsReportConfiguration) configuration).setRemoveEmptySpaceBetweenRows(Boolean.TRUE);
                // 一页一个sheet
                ((SimpleXlsReportConfiguration) configuration).setOnePagePerSheet(Boolean.FALSE);
                // 显示边框  背景白色
                ((SimpleXlsReportConfiguration) configuration).setWhitePageBackground(Boolean.FALSE);

                exporter.setConfiguration(configuration);
                break;
            case XLSX:
                exporter = new JRXlsxExporter();
                outPut = new SimpleOutputStreamExporterOutput((OutputStream) out);

                configuration = new SimpleXlsxReportConfiguration();

                ((SimpleXlsxReportConfiguration) configuration).setRemoveEmptySpaceBetweenRows(Boolean.TRUE);
                ((SimpleXlsxReportConfiguration) configuration).setRemoveEmptySpaceBetweenColumns(Boolean.TRUE);
                ((SimpleXlsxReportConfiguration) configuration).setWhitePageBackground(Boolean.FALSE);

                //自动选择格式
                ((SimpleXlsxReportConfiguration) configuration).setDetectCellType(Boolean.TRUE);
                exporter.setConfiguration(configuration);
                break;
            case XML:
                exporter = new JRXmlExporter();
                outPut = new SimpleXmlExporterOutput((PrintWriter) out);
                break;
//            case RTF:
//                exporter = new JRRtfExporter();
//                break;
            case CSV:
                exporter = new JRCsvExporter();
                outPut = new SimpleOutputStreamExporterOutput((OutputStream) out);
                break;
//            case DOC:
//                exporter = new JRRtfExporter();
//                break;
            case DOCX:
                exporter = new JRDocxExporter();
                outPut = new SimpleOutputStreamExporterOutput((OutputStream) out);
                break;
            case TXT:
                exporter = new JRTextExporter();
                outPut = new SimpleWriterExporterOutput((PrintWriter) out);
                configuration = new SimpleTextReportConfiguration();

                ((SimpleTextReportConfiguration) configuration).setCharWidth((float) 10);
                ((SimpleTextReportConfiguration) configuration).setCharHeight((float) 15);
                exporter.setConfiguration(configuration);
                break;
            default:
                exporter = new JRPdfExporter();
                outPut = new SimpleOutputStreamExporterOutput((OutputStream) out);
        }
        exporter.setExporterOutput(outPut);
        return exporter;
    }


    private JRAbstractExporter exporter;
    private JRAbstractLRUVirtualizer virtualizer;

    public boolean isSinglePage(String jasperPath) {
        File jasperFile = new File(jasperPath);
        return isSinglePage(jasperFile);
    }

    /**
     * 获取报表类型
     * @param jasperFile
     * @return
     * 0：普通多
     */
    public boolean isSinglePage(File jasperFile) {
        try {
            JasperReport jasperReport =  (JasperReport) JRLoader.loadObjectFromFile(jasperFile.getPath());
            for (JRParameter parameter : jasperReport.getParameters()) {
                if (parameter.getName().equalsIgnoreCase("isSinglePage")) {
                    return "1".equals(parameter.getDefaultValueExpression().getText());
                }
            }
        } catch (JRException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 生成不同格式报表文档(带缓存)
     *
     * @param docType    文档类型
     * @param jasperPath
     */
    public long createExportDocument(Object outputStream, DocType docType, String jasperPath, Map<String, Object> parameters, JRDataSource list) throws JRException, IOException {
        exporter = getJRExporter(docType, outputStream);

        //加缓存
//        parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
//        virtualizer.setReadOnly(true);

        exporter.setExporterInput(new SimpleExporterInput(getJasperPrintWithBeanList(jasperPath, parameters, list)));

        try {
            exporter.exportReport();
//            virtualizer.cleanup();
            return 0L;
        } finally {
            if (outputStream != null) {
                if (OutputStream.class.isInstance(outputStream)) {
                    ((OutputStream) outputStream).flush();
                    ((OutputStream) outputStream).close();
                }
                if (PrintWriter.class.isInstance(outputStream)) {
                    ((PrintWriter) outputStream).flush();
                    ((PrintWriter) outputStream).close();
                }
            }
        }
    }
//        public boolean createExportDocument (HttpServletResponse response, DocType docType, String
//        jasperPath, Map < String, Object > parameters, JRDataSource list,
//                String fileName) throws JRException, IOException, ServletException {
//            // 解决中文乱码问题
//            response.setCharacterEncoding("UTF-8");
//            response.setContentType(getContentType(docType));
//            response.setHeader("Content-Disposition",
//                    "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, "UTF-8"));
//
//
//            switch (docType) {
//                case XML:
//                    return createOutputStream(response.getWriter(), docType, jasperPath, parameters, list);
//                case HTML:
//                    return createOutputStream(response.getWriter(), docType, jasperPath, parameters, list);
//                default:
//                    return createOutputStream(response.getOutputStream(), docType, jasperPath, parameters, list);
//            }
////        return false;
////        if (ext.equals("csv") || ext.equals("doc") || ext.equals("rtf") || ext.equals("txt")) {
////
////            outWriter = response.getWriter();
////            SimpleWriterExporterOutput outPut = new SimpleWriterExporterOutput(outWriter);
////            exporter.setExporterOutput(outPut);
////            //exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, outWriter);
////        } else {
////            if (ext.equals("xml")) {
////                outWriter = response.getWriter();
////                XmlExporterOutput outPut = new SimpleXmlExporterOutput(outWriter);
////                exporter.setExporterOutput(outPut);
////            } else if (ext.equals("html")) {
////                outWriter = response.getWriter();
////                HtmlExporterOutput outPut = new SimpleHtmlExporterOutput(outWriter);
////                exporter.setExporterOutput(outPut);
////            } else {
////                outStream = response.getOutputStream();
////                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
////            }
////        }
////
////        try {
////            exporter.exportReport();
////            virtualizer.cleanup();
////        } catch (JRException e) {
////            throw new ServletException(e);
////        } finally {
////            if (outStream != null) {
////                try {
////                    outStream.close();
////                } catch (IOException ex) {
////                }
////            }
////            if (outWriter != null) {
////                outWriter.close();
////            }
////        }
//        }


        /**
         * 输出以分页的形式输出html
         *
         * @param jasperPrint
         * @param pageStr
         * @throws JRException
         * @throws IOException
         */

        public void createHtmlByPage(HttpServletResponse response, JasperPrint jasperPrint, String pageStr) throws
        JRException, IOException {
            int pageIndex = 0;
            int lastPageIndex = 0;

            HtmlExporter exporter = new HtmlExporter();
            if (null != jasperPrint.getPages()) {
                lastPageIndex = jasperPrint.getPages().size() - 1;
            }

            if (null == pageStr) {
                pageStr = "0";
            }
            try {
                pageIndex = Integer.valueOf(pageStr);
                if (pageIndex > 0) {
                    pageIndex = pageIndex - 1;
                }
            } catch (Exception e) {
                // 如果得到的非数字字符串
                if ("lastPage".equals(pageStr)) {
                    pageIndex = lastPageIndex;
                }
            }

            if (pageIndex < 0) {
                pageIndex = 0;
            }
            if (pageIndex > lastPageIndex) {
                pageIndex = lastPageIndex;
            }
            response.setCharacterEncoding("UTF-8");
            try {
                PrintWriter out = response.getWriter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

                SimpleHtmlReportConfiguration configuration = new SimpleHtmlReportConfiguration();
                configuration.setPageIndex(pageIndex);
                exporter.setConfiguration(configuration);
                //exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);

                HtmlExporterOutput outPut = new SimpleHtmlExporterOutput(out);
                exporter.setExporterOutput(outPut);

                exporter.exportReport();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }


//        /**
//         * 批量打印pdf文件
//         */
//        public void exportBatchPdf (List < JasperPrint > jasperPrintList, String fileName){
//
//            JRPdfExporter exporter = new JRPdfExporter();
//            try {
//                /**
//                 * 注入打印模板
//                 */
//                exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrintList));
//
//                OutputStream outStream = null;
//
//                response.setContentType(getContentType(DocType.PDF));
//                response.setHeader("Content-Disposition",
//                        "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, "UTF-8"));
//                outStream = response.getOutputStream();
//
//                exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
//                //配置项
//                SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
//                // 是否批量打印
//                configuration.setCreatingBatchModeBookmarks(true);
//                // 是否加密
//                configuration.setEncrypted(false);
//                exporter.setConfiguration(configuration);
//
//                exporter.exportReport();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//
        /**
         * 千分位格式化数据 保留两位小数，且 ‘0 ’ 转为 ‘--’
         *
         * @param obj
         * @param fieldNames 需转化的属性
         * @return
         */
        public Object toFormatNumber (Object obj, String[]fieldNames){
            Class clazz = (Class) obj.getClass();
            Field[] fs = clazz.getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                Field f = fs[i];
                // 设置些属性是可以访问的
                f.setAccessible(true);
                String type = f.getType().toString();
                Object val = null;

                try {
                    for (String str : fieldNames) {
                        if (f.getName() == str) {
                            val = f.get(obj);
                        }
                    }
                    if (null != val) {
                        if (type.endsWith("String")) {
                            if (val.equals("0")) {
                                f.set(obj, "--");
                            } else {
                                /*
                                 * ; BigDecimal str=new BigDecimal((String) val); DecimalFormat df=new
                                 * DecimalFormat(",###,##0.00");
                                 */ // 保留两位小数
                                f.set(obj, this.toNumeber((String) val));
                            }

                        } else if (type.endsWith("int") || type.endsWith("Integer")) {
                            // System.out.println(f.getType()+"\t");
                        } else {
                            // System.out.println(f.getType()+"\t");
                        }
                    }

                } catch (Exception ex) {
//                LOGGER.error("千分位格式化数据错误" + ex.getMessage(), ex);
                }
            }
            return obj;
        }

        /**
         * 转为万元保留小数点后两位
         *
         * @param value
         * @return
         */
        private String toNumeber (String value){
            Double number = Double.valueOf(value) / 10000.00;
            BigDecimal str = new BigDecimal(number);
            DecimalFormat df = new DecimalFormat(",###,##0.00");

            return df.format(str);
        }


    }