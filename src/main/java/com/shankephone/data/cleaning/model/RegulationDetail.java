package com.shankephone.data.cleaning.model;

/**
 * 列规则定义
 * @author fengql
 * @version 2018年3月29日 下午8:55:07
 */
public class RegulationDetail {
	
	private String id;
	private String regulationId;
	private String colName;
	private String colValue;
	private String operator;
	private String sqlTxt;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRegulationId() {
		return regulationId;
	}
	public void setRegulationId(String regulationId) {
		this.regulationId = regulationId;
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
