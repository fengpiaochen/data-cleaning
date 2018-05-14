package com.shankephone.data.cleaning.model;

import java.util.Date;

/**
 * 数据库名和表规则定义
 * @author fengql
 * @version 2018年3月29日 下午8:53:37
 */
public class Regulation {
	
	private String id;
	private String sourceId;
	private String dbName;
	private String tableName;
	private String name;
	private String colName;
	private String colValue;
	private String operator;
	private String sqlTxt;
	private Date createTime;
	private Date modifyTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public String getColValue() {
		return colValue;
	}
	public void setColValue(String colValue) {
		this.colValue = colValue;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getSqlTxt() {
		return sqlTxt;
	}
	public void setSqlTxt(String sqlTxt) {
		this.sqlTxt = sqlTxt;
	}
	
	

}
