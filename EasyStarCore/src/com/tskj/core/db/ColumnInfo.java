package com.tskj.core.db;

public class ColumnInfo {

	private String columnName ;
/**
 * 列名
 * */
	public String getColumnName () {
		return columnName ;
	}

	public void setColumnName (String colName) {
		columnName = colName;
	}
	
	private int size;
/**
 * 大小
 * */
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	private int columnType;
/**
 * 数据类型
 * */
	public int getColumnType() {
		return columnType;
	}

	public void setColumnType(int columnType) {
		this.columnType = columnType;
	}

}
