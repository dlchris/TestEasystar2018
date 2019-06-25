package com.tskj.classtree.bean;

/**
 * @author LeonSu
 */
public class ClassTreeInfo {
    private String mapClassId = "";
    private String realClassId = "";
    private int classLevel = 0;
    private int classType = 0;
    private String parentClassId = "";
    private String description = "";
    private String perFixDes = "";
    private String docTable = "";
    private String boxTable = "";
    private String roolTable = "";
    private String perFixStream = "";

    public String getRealClassId() {
        return realClassId;
    }

    public void setRealClassId(String realClassId) {
        this.realClassId = realClassId;
    }

    public String getMapClassId() {
        return mapClassId;
    }

    public void setMapClassId(String classId) {
        this.mapClassId = classId;
    }

    public int getClassLevel() {
        return classLevel;
    }

    public void setClassLevel(int classLevel) {
        this.classLevel = classLevel;
    }

    public int getClassType() {
        return classType;
    }

    public void setClassType(int classType) {
        this.classType = classType;
    }

    public String getParentClassId() {
        return parentClassId;
    }

    public void setParentClassId(String parentClassId) {
        this.parentClassId = parentClassId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPerFixDes() {
        return perFixDes;
    }

    public void setPerFixDes(String perFixDes) {
        this.perFixDes = perFixDes;
    }

    public String getDocTable() {
        return docTable;
    }

    public void setDocTable(String docTable) {
        this.docTable = docTable;
    }

    public String getBoxTable() {
        return boxTable == null ? "" : boxTable;
    }

    public void setBoxTable(String boxTable) {
        this.boxTable = boxTable;
    }

    public String getRoolTable() {
        return roolTable == null ? "" : roolTable;
    }

    public void setRoolTable(String roolTable) {
        this.roolTable = roolTable;
    }

    public String getPerFixStream() {
        return perFixStream;
    }

    public void setPerFixStream(String perFixStream) {
        this.perFixStream = perFixStream;
    }
}
