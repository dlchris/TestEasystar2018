package com.tskj.fileimport.system;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLDecoder;

public class Config {
    /**
     * 修改配置文件中的属性值
     * @param bundleName
     *      配置文件名，比如：system.properties
     * @param keyName
     *      属性的名称
     * @param keyValue
     *      属性的值
     * @return
     *      true：修改成功；false：修改失败
     */
    public static boolean setConfigValue(String bundleName, String keyName, String keyValue) {
        // 获取src下的文件路径
        String propertiesFileName = Tools.getClassPath().concat(bundleName);
        boolean writeOK = true;
        SafeProperties p = new SafeProperties();
        FileInputStream in;
        try {
            in = new FileInputStream(propertiesFileName);
            p.load(in);
            in.close();
            // 设置属性值，如不属性不存在新建
            p.setProperty(keyName, keyValue);
            // 输出流
            FileOutputStream out = new FileOutputStream(propertiesFileName);
            // 设置属性头，如不想设置，请把后面一个用""替换掉
            p.store(out);
            // 清空缓存，写入磁盘
            out.flush();
            // 关闭输出流
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            writeOK = false;
        }
        return writeOK;
    }

    /**
     * 修改配置文件（system.properties）中的属性值
     * @param keyName
     *      属性的名称
     * @param keyValue
     *      属性的值
     * @return
     *      true：修改成功；false：修改失败
     */
    public static boolean setConfigValue(String keyName, String keyValue) {
        return setConfigValue("system.properties", keyName, keyValue);
    }

    /**
     * 获取配置文件中的值
     *
     * @param bundleName 配置文件的名称，不需要扩展名，默认的扩展名是：properties
     * @param keyName    key
     * @return
     */
    public static String getConfigValue(String bundleName, String keyName) {
        String propertiesFileName = Tools.getClassPath().concat(bundleName);
        SafeProperties p = new SafeProperties();
        FileInputStream in;
        try {
            in = new FileInputStream(propertiesFileName);
            p.load(in);
            in.close();
            return p.getProperty(keyName, "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
//
//        ResourceBundle res = ResourceBundle.getBundle(bundleName);
//        if (res.containsKey(keyName)) {
//            try {
//                return new String(res.getString(keyName.trim()).getBytes("ISO-8859-1"), "GBK");
//            } catch (UnsupportedEncodingException e) {
//                return "";
//            }
//        }
//        return "";
    }

    /**
     * 获取配置文件（system.properties）中的值
     *
     * @param keyName key
     * @return
     */
    public static String getConfigValue(String keyName) {
        return getConfigValue("system.properties", keyName);
    }

}
