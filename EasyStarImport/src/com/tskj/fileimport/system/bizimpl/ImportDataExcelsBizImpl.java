package com.tskj.fileimport.system.bizimpl;

import com.alibaba.fastjson.JSONObject;
import com.tskj.fileimport.system.biz.ImportDataBiz;
import com.tskj.fileimport.system.consts.DataTypeConsts;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @notes:
 * @program: easystar2018
 * @author: JRX
 * @create: 2019-03-15 10:08
 **/
public class ImportDataExcelsBizImpl implements ImportDataBiz {
    private String xlsFileName;
    private String xlsFilePath;
    private File xlsFile;
    private Sheet sheet1;// 数据对象
    private Row firstRow;// excal表头列 数据(第一行数据)

    //初始化就获取对应的excal数据
    public ImportDataExcelsBizImpl(String xlsFileName, String path) {
        this.xlsFileName = xlsFileName;
        this.xlsFilePath = path;
        try {

            xlsFile = new File(URLDecoder.decode(this.getClass().getResource("/").getPath(), "utf-8")
                    + path + xlsFileName);
            //数据初始化
            fieldsInIt();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public ImportDataExcelsBizImpl(File xlsFile) {
        this.xlsFile = xlsFile;
    }

    /**
     * MethodName:getColumnValue
     * <p>
     * Description:获得指定行的所有列的值
     * </p>
     *
     * @param row 行对象
     * @return 每列的list集合
     * @author DuXiao
     */
    private List<String> getColumnValue(Row row) {
        List<String> list = new ArrayList<>();
        for (int n = 0; n < row.getPhysicalNumberOfCells(); n++) {
            // 逐列循环
            list.add(row.getCell(n).getStringCellValue());
            // 读第一行，为字段属性
        }
        return list;
    }

    private String getStringCellValue(Cell cell) {// 获取单元格数据内容为字符串类型的数据
        String strCell = "";
        if (cell == null) {
            return "";
        }
        switch (cell.getCellTypeEnum()) {
            case FORMULA:
                try {
                    strCell = String.valueOf(cell.getStringCellValue());
                    //System.err.println(strCell);
                } catch (IllegalStateException e) {
                    //System.err.println("报错第几行:"+cell.getColumnIndex());
                    System.err.println(e.getMessage());
                    //strCell = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case STRING:
                strCell = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    strCell = getDateCellValue(cell);
                    break;
                } else {
                    Long longVal = Math.round(cell.getNumericCellValue());
                    Double doubleVal = cell.getNumericCellValue();
                    if (Double.parseDouble(longVal + ".0") == doubleVal) {
                        // 判断是否含有小数位.0
                        strCell = String.valueOf(longVal);
                    } else {
                        strCell = String.valueOf(doubleVal);
                    }
                    break;
                }
            case BOOLEAN:
                strCell = String.valueOf(cell.getBooleanCellValue());
                break;
            default:
                strCell = "";
                break;
        }
        return strCell;
    }

    private String getDateCellValue(Cell cell) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        double value = cell.getNumericCellValue();
        Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
        return sdf.format(date);
    }

    private void closeFile(InputStream in, Workbook wb) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (wb != null) {
//            try {
//                wb.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }


    @Override
    public List<String> getColumns() {
        List<String> list = new ArrayList<>();
        InputStream in = null;
        // 工作簿
        Workbook wb = null;
        // sheet表
        Sheet sheet;
        try {
            in = new FileInputStream(xlsFile);
            // 工作簿
            wb = WorkbookFactory.create(in);
            // sheet表
            sheet = wb.getSheetAt(0);
            // 行
            Row row = sheet.getRow(0);
            list = getColumnValue(row);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        } finally {
            closeFile(in, wb);
        }
        return list;
    }

    @Override
    public int getSizeCount() {
        InputStream in = null;
        // 工作簿
        Workbook wb = null;
        // sheet表
        Sheet sheet;
        int count = 0;
        try {
            in = new FileInputStream(xlsFile);
            // 工作簿
            wb = WorkbookFactory.create(in);
            // sheet表
            sheet = wb.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            int firstRowNum = sheet.getFirstRowNum();
            for (int rowIndex = firstRowNum; rowIndex < lastRowNum; rowIndex++) {
                if (isRowEmpty(sheet.getRow(rowIndex))) {//为true有空行
                    //有空行默认导入截止位置是这里
                    return rowIndex - 1;//不减1当前空行也算在内了容易后边出现空指针
                }
            }
            //如果没有空行那么默认导入到总行数
            count = lastRowNum;
            //System.err.println(count);
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        } finally {
            closeFile(in, wb);
        }
        return count;
    }

    @Override
    public int getSize() {
        InputStream in = null;
        // 工作簿
        Workbook wb = null;
        // sheet表
        Sheet sheet;
        int count = 0;
        try {
            in = new FileInputStream(xlsFile);
            // 工作簿
            wb = WorkbookFactory.create(in);
            // sheet表
            sheet = wb.getSheetAt(0);
            count = sheet.getLastRowNum();
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        } finally {
            closeFile(in, wb);
        }
        return count;
    }

    @Override
    public boolean isRowEmpty(Row row) {
        //判断是否是空行
        if (null == row) {
            return true;//当前是null 空行返回true
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            //字段不为空就返回false,全部为空返回true
            if (cell != null && cell.getCellTypeEnum() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }


    public void fieldsInIt() {
        InputStream in = null;
        //System.err.println("excal初始化开始:" + xlsFile);
        // 工作簿
        Workbook wb = null;
        try {
            in = new FileInputStream(xlsFile);
            // 工作簿
            wb = WorkbookFactory.create(in);
            // sheet表
            sheet1 = wb.getSheetAt(0);
            firstRow = sheet1.getRow(0);

            //System.err.println("excal初始化完毕");
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        } finally {
            closeFile(in, wb);
        }
    }

    @Override
    public Map<String, Object> getData(int index) {
        List<String> cols = getColumnValue(firstRow);
        Row row = sheet1.getRow(index);
        Map<String, Object> map = new HashMap<>(10);
        for (int n = 0; n < cols.size(); n++) {
            // 逐列循环
            String colName = cols.get(n);
            String cellValue = getStringCellValue(row.getCell(n));
            map.put(colName, cellValue);
        }
           /* long endTime = System.currentTimeMillis();
            endTime = endTime;
            System.err.println("流时间:" + (endTime - startTime)+"ms");*/
        //System.err.println(map);
        return map;
    }

    @Override
    public List<Map<String, Object>> getDataAll() {
        return null;
    }

    @Override
    public Object save(int index) {
        return null;
    }

    @Override
    public boolean deleteFile() {
        if (xlsFile.exists()) {
            return xlsFile.delete();
        }
        return true;
    }

    @Override
    public boolean verfiy(String fieldName) {
        int colIndex = getIndex(sheet1, fieldName);
        int firstRowNum = sheet1.getFirstRowNum();
        //TODO 是否替换成有数据的行数 getSizeCount()
        int lastRowNum = sheet1.getLastRowNum();
        Row row;
        Set<String> sett = new HashSet<>();
        //int rowIndex = firstRowNum+1;第一行应该在验证范围内?
        for (int rowIndex = firstRowNum; rowIndex < lastRowNum; rowIndex++) {
            row = sheet1.getRow(rowIndex);
            if (row == null) {//行为空必须解决否则报空指针
                return true;//TODO 没想好怎么解决呢 ,目前有空行就不解决了 只判断到空行之前的行是否有重复
            }
            String res = getStringCellValue(row.getCell(colIndex));

            boolean a = true;
            if (!"".equals(res)) {//不为空继续判定

                a = sett.add(res);
            } else {//主键为空 直接舍弃后面所有对象  告知用户主键ID有空行
                System.err.println("第几行为空：" + rowIndex);
                return false;
            }
            //出现重复
            if (!a) {
                System.err.println("有重复主键！！！");
                return false;
            }
        }
        return true;
    }

    @Override
    public JSONObject verifyDataType(String sourceFieldName, DataTypeConsts fieldType, int fieldSize, String targetFieldName) {
        JSONObject ret = JSONObject.parseObject("{}");
        int colIndex = getIndex(sheet1, sourceFieldName);
        int firstRowNum = sheet1.getFirstRowNum() + 1;
        int lastRowNum = sheet1.getLastRowNum();
        Row row;
        String cellValue;
        ret.put("result", 0);
        ret.put("errmsg", "");
        for (int rowIndex = firstRowNum; rowIndex < lastRowNum; rowIndex++) {
            row = sheet1.getRow(rowIndex);
            if (row == null) {//行位空必须解决否则报空指针,有空行直接验证结束
                return ret;//TODO 没想好怎么解决呢 ,目前有空行就不解决了 只判断到空行之前的行是否有重复
            }

            cellValue = getStringCellValue(row.getCell(colIndex));
            switch (fieldType) {
                case DT_STRING:
                    if (String_Length(cellValue) > fieldSize) {
                        ret.put("result", 1);
                        ret.put("errmsg", String.format("字段（%s），第%d条记录，来源内容超长，最长%d位", sourceFieldName, rowIndex, fieldSize));
                        return ret;
                    }
                    break;
                case DT_INTEGER:
                    if (!StringUtils.isNumeric(cellValue)) {
                        ret.put("result", 1);
                        ret.put("errmsg", String.format("字段（%s），第%d条记录，来源数据类型不正确，含有非数字字符", sourceFieldName, rowIndex));
                        return ret;
                    }
                    break;
                case DT_DATETIME:
                    if (String_Length(cellValue) > fieldSize) {
                        ret.put("result", 1);
                        ret.put("errmsg", String.format("字段（%s），第%d条记录，日期超长，最长8位", sourceFieldName, rowIndex));
                        return ret;
                    }
                    break;
                default:
                    ret.put("result", 1);
                    ret.put("errmsg", "未知数据类型");
                    break;
            }
        }
        return ret;
    }

    private int getIndex(Sheet sheet, String fieldName) {
        List<String> cols = getColumnValue(sheet.getRow(0));
        for (int n = 0; n < cols.size(); n++) {
            // 逐列循环
            String colName = cols.get(n);
            if (colName.equals(fieldName)) {
                return n;
            }
        }
        return -1;
    }

    public static int String_Length(String value) {
        int valueRel = 0;
        String chinese = "[\u4e00-\u9fa5]";
        for (int i = 0; i < value.length(); i++) {
            String tempStr = value.substring(i, i + 1);
            if (tempStr.matches(chinese)) {
                valueRel += 2;
            } else {
                valueRel += 1;
            }
        }
        return valueRel;
    }


    public static void main(String[] args) {
        //Map<Object, Object> map =


        //System.out.println(ImportDataFactory.getImortDataImpl("20181012134206029.xls", "..\\..\\TempImportFile\\").getData(20));
        String s = "few改为00";
        System.err.println(String_Length(s));
    }
}
