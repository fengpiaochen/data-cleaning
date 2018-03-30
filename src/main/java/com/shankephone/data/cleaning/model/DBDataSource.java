package com.shankephone.data.cleaning.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 数据源配置规则
 * @author fengql
 * @version 2018年3月30日 上午12:47:16
 */
public class DBDataSource {
	
	private String id;
	
	private String masterUrl;
	
	private String slaveUrl;
	
	private String masterUsername;
	
	private String masterPassword;
	
	private String slaveUsername;
	
	private String slavePassword;

	private JdbcTemplate masterJdbc;
	
	private JdbcTemplate slaveJdbc;
	
	private List<Regulation> regulations = new ArrayList<Regulation>();
	
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

	public String getSlaveUrl() {
		return slaveUrl;
	}

	public void setSlaveUrl(String slaveUrl) {
		this.slaveUrl = slaveUrl;
	}

	public JdbcTemplate getMasterJdbc() {
		return masterJdbc;
	}

	public void setMasterJdbc(JdbcTemplate masterJdbc) {
		this.masterJdbc = masterJdbc;
	}

	public JdbcTemplate getSlaveJdbc() {
		return slaveJdbc;
	}

	public void setSlaveJdbc(JdbcTemplate slaveJdbc) {
		this.slaveJdbc = slaveJdbc;
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

	public List<Regulation> getRegulations() {
		return regulations;
	}

	public void setRegulations(List<Regulation> regulations) {
		this.regulations = regulations;
	}

	
	

}
