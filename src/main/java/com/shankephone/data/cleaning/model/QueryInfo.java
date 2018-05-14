package com.shankephone.data.cleaning.model;

import com.shankephone.data.cleaning.common.JdbcDaoFactory;

/**
 * 从库查询、主库删除信息
 * @author fengql
 * @version 2018年3月30日 上午12:47:44
 */
public class QueryInfo {
	//数据源ID
	private String id;
	
	private String masterUrl;
	private String masterUsername;
	private String masterPassword;
	
	private String slaveUrl;
	private String slaveUsername;
	private String slavePassword;
	
	private String dbName;
	
	private String slaveSQL;
	private String masterSQL;
	private String checkSQL;
	
	private JdbcDaoFactory slaveDao;
	private JdbcDaoFactory masterDao;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMasterUrl() {
		return masterUrl;
	}

	public void setMasterUrl(String masterUrl) {
		this.masterUrl = masterUrl;
	}

	public String getMasterUsername() {
		return masterUsername;
	}

	public void setMasterUsername(String masterUsername) {
		this.masterUsername = masterUsername;
	}

	public String getMasterPassword() {
		return masterPassword;
	}

	public void setMasterPassword(String masterPassword) {
		this.masterPassword = masterPassword;
	}

	public String getSlaveUrl() {
		return slaveUrl;
	}

	public void setSlaveUrl(String slaveUrl) {
		this.slaveUrl = slaveUrl;
	}

	public String getSlaveUsername() {
		return slaveUsername;
	}

	public void setSlaveUsername(String slaveUsername) {
		this.slaveUsername = slaveUsername;
	}

	public String getSlavePassword() {
		return slavePassword;
	}

	public void setSlavePassword(String slavePassword) {
		this.slavePassword = slavePassword;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getSlaveSQL() {
		return slaveSQL;
	}

	public void setSlaveSQL(String slaveSQL) {
		this.slaveSQL = slaveSQL;
	}

	public String getMasterSQL() {
		return masterSQL;
	}

	public void setMasterSQL(String masterSQL) {
		this.masterSQL = masterSQL;
	}

	public JdbcDaoFactory getSlaveDao() {
		return slaveDao;
	}

	public void setSlaveDao(JdbcDaoFactory slaveDao) {
		this.slaveDao = slaveDao;
	}

	public JdbcDaoFactory getMasterDao() {
		return masterDao;
	}

	public void setMasterDao(JdbcDaoFactory masterDao) {
		this.masterDao = masterDao;
	}

	public String getCheckSQL() {
		return checkSQL;
	}

	public void setCheckSQL(String checkSQL) {
		this.checkSQL = checkSQL;
	}
	
	

}
