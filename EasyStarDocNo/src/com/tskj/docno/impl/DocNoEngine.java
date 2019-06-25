package com.tskj.docno.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tskj.docno.bean.DocNoFieldInfo;
import com.tskj.docno.dao.DocNoDao;

import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DocNoEngine {

    private Vector<DocNoFieldInfo> docNoRule;

    public Vector<DocNoFieldInfo> getDocNoRule() {
        return docNoRule;
    }

    public void setDocNoRule(String classType, String docNoRuleString) {
        docNoRule.clear();
        String[] tmpList = docNoRuleString.split("\\+");
        DocNoFieldInfo tmpInfo;
        int tmpPos;
        for (String tmpValue : tmpList) {
            //classType  看EasyStarConsts类 0.文件级 1.盒级 2.案卷级
            if (!classType.equals("0")) {// classtype 一定要用session 中获取的classtype 不能是classTreeInfo中的
                if (tmpValue.toUpperCase().indexOf("NOTENO") >= 0) {
                    continue;
                }
            }
            if (tmpValue.isEmpty()) {
                continue;
            }
            tmpInfo = new DocNoFieldInfo();
            tmpPos = tmpValue.indexOf("(");
            if (tmpPos >= 0) {
                tmpInfo.setFieldName(tmpValue.substring(0, tmpPos));
                tmpInfo.setDelimiter(tmpValue.substring(tmpPos + 1, tmpPos + 2));
                tmpInfo.setDictFieldName("");
            } else {
                tmpInfo.setFieldName(tmpValue);
                tmpInfo.setDelimiter("-");
                tmpInfo.setDictFieldName("");
            }
            tmpPos = tmpInfo.getFieldName().indexOf("_ID");
            if (tmpPos >= 0) {
                tmpInfo.setIsDictField(true);
                tmpInfo.setDictFieldName(tmpInfo.getFieldName());
                tmpInfo.setFieldName(tmpInfo.getFieldName().substring(0, tmpPos));
            } else {
                tmpInfo.setIsDictField(false);
            }
            docNoRule.add(tmpInfo);
        }
    }

    public DocNoEngine(String tableName, String classType, String docNoRuleString) {
        this(classType, docNoRuleString);
        DocNoDao docNoDao = new DocNoDao();
        List<Map<String, Object>> list = docNoDao.getFieldsSize(tableName);
        for (DocNoFieldInfo info : docNoRule) {
            for (Map<String, Object> map : list) {
                if (map.get("fieldname").toString().trim().equalsIgnoreCase(info.getFieldName())) {
                    info.setFieldSize(Integer.valueOf(map.get("fieldsize").toString()));
                }
                if (info.getIsDictField()) {
                    if (map.get("fieldname").toString().trim().equalsIgnoreCase(info.getDictFieldName())) {
                        info.setDictFieldSize(Integer.valueOf(map.get("fieldsize").toString()));
                    }
                }
            }
        }
    }

    public DocNoEngine(String classType, String docNoRuleString) {
        docNoRule = new Vector<>();
        setDocNoRule(classType, docNoRuleString);
    }

    public String getNewDocNoByMap(Map<String, Object> value, boolean autoFill) {
        return getNewDocNoByMap(value, false);
    }

    public String getNewDocNoByMap(Map<String, Object> value) {
        JSONObject tmpValue = JSONObject.parseObject(JSON.toJSONString(value));
        return getNewDocNoByJson(tmpValue);
    }

    public String getNewDocNoByJson(JSONObject value, boolean autoFill) {
        StringBuilder newDocNo = new StringBuilder("");
        String tmpStr;
        /**
         * 是否进行长度的判断
         */
        int tmpLen;
        for (DocNoFieldInfo info : docNoRule) {
            tmpStr = "";
            if (info.getIsDictField()) {
                //如果是字典项
                tmpLen = info.getDictFieldSize();
                String DictFieldName = value.getString(info.getDictFieldName());
                if (value.containsKey(info.getDictFieldName()) && DictFieldName != null && !DictFieldName.isEmpty()) {
                    tmpStr = value.getString(info.getDictFieldName()).trim();
                }
            } else {
                //如果不是字典项
                tmpLen = info.getFieldSize();
                String fieldName = value.getString(info.getFieldName());
                if (value.containsKey(info.getFieldName()) && fieldName != null && !fieldName.isEmpty()) {
                    tmpStr = value.getString(info.getFieldName()).trim();
                }
            }

            if (tmpStr.length() < tmpLen) {
                //如果字段内容的长度小于字段长度
                if (tmpStr.isEmpty()) {
                    //如果字段内容为空，则要判断是否需要自动补0
                    if (!autoFill) {
                        //如果不允许自动填充，则返回空白档号
                        return "";
                    }
                    //如果允许自动填充
                    if (!info.getIsDictField()) {
                        //如果不是字典项
                        if (tmpStr.isEmpty()) {
                            tmpStr = String.format("%0" + tmpLen + "d", 0);
                        } else {
                            tmpStr = fillStr(tmpStr, tmpLen);
                        }
                    } else {
                        if (tmpStr.isEmpty()) {
                            tmpStr = String.format("%0" + tmpLen + "d", 0);
                        }
                    }
                } else {
                    if (!info.getIsDictField()) {
                        tmpStr = fillStr(tmpStr, tmpLen);
                    }
                    //System.err.println(tmpStr);
                }
            } else {
                if (tmpStr.length() > tmpLen) {
                    //如果字段内容的长度大于字段长度，则自动从右至左取子串
                    tmpStr = tmpStr.substring(tmpStr.length() - tmpLen, tmpStr.length());
                }
            }
            newDocNo.append(String.format("%s%s", tmpStr, info.getDelimiter()));

        }
        String result = newDocNo.toString().toUpperCase();
        //System.err.println(result.substring(0, result.length() - 1));
        return result.substring(0, result.length() - 1);
    }

    public String getNewDocNoByJson(JSONObject value) {
        return getNewDocNoByJson(value, false);
    }

    public static String fillStr(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                // 左补0
                sb.append("0").append(str);
                // sb.append(str).append("0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    @Override
    public String toString() {
        StringBuilder strReturn = new StringBuilder("");
        for (DocNoFieldInfo info : docNoRule) {
            if (info.getIsDictField()) {
                strReturn.append(String.format("%s(%s)+", info.getDictFieldName(), info.getDelimiter()));
            } else {
                strReturn.append(String.format("%s(%s)+", info.getFieldName(), info.getDelimiter()));
            }
        }
        String result = strReturn.toString();
        return result.substring(0, result.length() - 1);
    }

    public boolean add(DocNoFieldInfo fieldInfo) {
        if (docNoRule.contains(fieldInfo)) {
            return false;
        }
        return docNoRule.add(fieldInfo);
    }

    public boolean remove(DocNoFieldInfo fieldInfo) {
        if (!docNoRule.contains(fieldInfo)) {
            return false;
        }
        return docNoRule.remove(fieldInfo);
    }

    public DocNoFieldInfo remove(int index) {
        return docNoRule.remove(index);
    }

    public void clear() {
        docNoRule.clear();
    }

    /**
     * 判断字段是不是档号组成项
     *
     * @param fieldName 字段名
     * @return true：是，false：不是
     * @returnStartImportAction.do true：是，false：不是
     */
    public boolean indexOf(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        for (DocNoFieldInfo info : docNoRule) {
            if (info.getFieldName().equalsIgnoreCase(fieldName) || info.getDictFieldName().equalsIgnoreCase(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param fieldName
     * @return
     * @Author JRX
     * @Description: 判断档号组成项是否是字典项
     * @create 2019/3/10 19:05
     **/

    public boolean indexOfDic(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }
        fieldName = fieldName + "_ID";
        for (DocNoFieldInfo info : docNoRule) {
            if (info.getDictFieldName().equalsIgnoreCase(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        DocNoEngine docNoEngine = new DocNoEngine("DOCUMENT88050C556E4E4988", "0",
                "ALLNO(-)+CLASSTYPE_ID(·)+YEARNO(-)+FIELD0B546940487B4AAA_ID(-)+DEPARTMENT_ID(-)+NOTENO(-)");
        JSONObject value = JSONObject.parseObject("{}");
        value.put("ALLNO", "123");
        value.put("CLASSTYPE_ID", "1");
        value.put("YEARNO", "12345");
        value.put("FIELD0B546940487B4AAA_ID", "YJ");
        value.put("DEPARTMENT_ID", "0001");
        value.put("NOTENO", "001");
        System.out.println(docNoEngine.getNewDocNoByJson(value, true));
    }
}
