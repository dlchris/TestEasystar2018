package com.tskj.fileimport.system.bizimpl;

import com.tskj.fileimport.system.biz.ImportDataBiz;

/**
 * 数据导入的工厂类，简单工厂模式，
 * 根据文件扩展名创建相对应的文件信息处理实例
 * 简单工厂模式：类似于定制商品，由消费者告知工厂定制生产何种商品
 *
 * @author LeonSu
 */
public class ImportDataFactory {

    /**
     * 根据文件扩展名，得到相应的处理类
     *
     * @param fileName 文件名
     * @param path     文件所在目录
     * @return
     */
    public static ImportDataBiz getImortDataImpl(String fileName, String path) {
        int len = fileName.length();
        int index = fileName.lastIndexOf('.');
        String fileExt = fileName.substring(index + 1, len).toLowerCase();
        switch (fileExt) {
            case "xls":
                //return new ImportDataExcelBizImpl(fileName, path);
                return new ImportDataExcelsBizImpl(fileName, path);
            case "xlsx":
                //return new ImportDataExcelBizImpl(fileName, path);
                return new ImportDataExcelsBizImpl(fileName, path);
            default:
                return null;
        }
    }
}
