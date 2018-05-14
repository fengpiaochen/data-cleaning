package com.shankephone.data.cleaning.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 数据源配置规则
 * @author fengql
 * @version 2018年3月30日 上午12:47:16
 */
@Getter
@Setter
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
	
	private String historyUrl;
	
	private Map<String,List<Regulation>> regulations = new HashMap<String,List<Regulation>>();

}
