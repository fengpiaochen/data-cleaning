package com.shankephone.data.cleaning.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	private Date createTime;
	private Date modifyTime;
	
	private List<RegulationDetail> details = new ArrayList<RegulationDetail>();
	
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
	public List<RegulationDetail> getDetails() {
		return details;
	}
	public void setDetails(List<RegulationDetail> details) {
		this.details = details;
	}
	
	

}
