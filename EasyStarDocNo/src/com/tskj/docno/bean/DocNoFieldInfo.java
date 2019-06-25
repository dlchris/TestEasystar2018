package com.tskj.docno.bean;

public class DocNoFieldInfo {

    private String fieldName;
    private String dictFieldName;
    private String delimiter;
    private int fieldSize;
    private int dictFieldSize;
    private boolean isDictField;

    /**
     * 是否是字典项
     * @return
     */
    public boolean getIsDictField() {
        return isDictField;
    }

    /**
     * 是否是字典项
     * @param isDictField
     */
    public void setIsDictField(boolean isDictField) {
        this.isDictField = isDictField;
    }

    /**
     * 字典字段
     * @return
     */
    public String getDictFieldName() {
        return dictFieldName;
    }

    /**
     * 设置字典字段
     * @param dictFieldName
     */
    public void setDictFieldName(String dictFieldName) {
        this.dictFieldName = dictFieldName;
    }

    /**
     * 字段名称
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * 字段名称
     * @param fieldName
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * 档号组成项分隔符
     * @return
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * 档号组成项分隔符，“·”
     * @param delimiter
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * 得到字段长度
     * @return
     */
    public int getFieldSize() {
        return fieldSize;
    }

    /**
     * 设置字段长度
     * @param fieldSize
     */
    public void setFieldSize(int fieldSize) {
        this.fieldSize = fieldSize;
    }

    /**
     * 得到字段的字典字段长度
     * @return
     */
    public int getDictFieldSize() {
        return dictFieldSize;
    }

    /**
     * 设置字段的字典字段长度
     * @param dictFieldSize
     */
    public void setDictFieldSize(int dictFieldSize) {
        this.dictFieldSize = dictFieldSize;
    }
}
