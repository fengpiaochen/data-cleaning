package com.shankephone.data.cleaning;


import java.io.IOException;

import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.shankephone.data.cleaning.common.JdbcDaoFactory;
import com.shankephone.data.cleaning.model.DBDataSource;

public class TestDB {
	
	private String url = "jdbc:mysql://172.10.3.165:3306/?useUnicode=true&amp;characterEncoding=utf-8";
	private String username = "root";
	private String password = "!QAZxcvfr432";
	private JdbcTemplate jdbcTemplate;
	private Properties properties = new Properties();
	public TestDB(){
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
			MysqlDataSource dataSource = new MysqlDataSource();
			dataSource.setUrl(properties.getProperty("dataSource.url"));
			dataSource.setUser(properties.getProperty("dataSource.username"));
			dataSource.setPassword(properties.getProperty("dataSource.password"));
			jdbcTemplate = new JdbcTemplate(dataSource);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询所有数据源
	 * @param sql
	 * @return
	 */
	public List<Map<String,Object>> queryList(String sql){
		return jdbcTemplate.queryForList(sql);
	}
	
	/**
	 * 根据查询的数据源结果创建数据库连接
	 * @param records
	 * @return
	 */
	public static List<DBDataSource> createDataSource(List<Map<String,Object>> records){
		List<DBDataSource> list = new ArrayList<DBDataSource>();
		for(Map<String,Object> record : records){
			DBDataSource wrapper = new DBDataSource();
			
			String masterUrl = String.valueOf(record.get("masterUrl"));
			String slaveUrl = String.valueOf(record.get("slaveUrl"));
			
			MysqlDataSource masterDataSource = new MysqlDataSource();
			masterDataSource.setUrl(String.valueOf(record.get("master_url")));
			masterDataSource.setUser(String.valueOf(record.get("master_username")));
			masterDataSource.setPassword(String.valueOf(record.get("master_password")));
			JdbcTemplate masterJdbc = new JdbcTemplate(masterDataSource);
			
			MysqlDataSource slaveDataSource = new MysqlDataSource();
			slaveDataSource.setUrl(String.valueOf(record.get("slave_url")));
			slaveDataSource.setUser(String.valueOf(record.get("slave_username")));
			slaveDataSource.setPassword(String.valueOf(record.get("slave_password")));
			JdbcTemplate slaveJdbc = new JdbcTemplate(slaveDataSource);
			
			wrapper.setId(String.valueOf(record.get("id")));
			wrapper.setMasterUrl(masterUrl);
			wrapper.setMasterUsername(String.valueOf(record.get("master_username")));
			wrapper.setMasterPassword(String.valueOf(record.get("master_password")));
			
			wrapper.setSlaveUrl(slaveUrl);
			wrapper.setSlaveUsername(String.valueOf(record.get("slave_username")));
			wrapper.setSlavePassword(String.valueOf(record.get("slave_password")));
			
			wrapper.setMasterJdbc(masterJdbc);
			wrapper.setSlaveJdbc(slaveJdbc); 
			
			list.add(wrapper);
		}
		return list;
	}
	
	/**
	 * 获取某一数据源的数据库
	 * @param jdbcTemplate
	 * @return
	 */
	public static List<String> getDatabases(JdbcTemplate jdbcTemplate){
		List<String> databases = new ArrayList<String>();
		List<Map<String,Object>> maps = jdbcTemplate.queryForList("show databases");
		for(Map<String,Object> map : maps){
			Set<String> keySet = map.keySet();
			for(String key : keySet){
				databases.add(String.valueOf(map.get(key)));
			}
		}
		return databases;
	}
	
	/**
	 * 查询指定数据库的所有表
	 * @param jdbcTemplate
	 * @param dbName
	 * @return
	 */
	public static List<String> getTables(JdbcTemplate jdbcTemplate, String dbName){
		List<String> tables = new ArrayList<String>();
		List<Map<String,Object>> maps = jdbcTemplate.queryForList("show tables");
		for(Map<String,Object> map : maps){
			Set<String> keySet = map.keySet();
			for(String key : keySet){
				tables.add(String.valueOf(map.get(key)));
			}
		}
		return tables;
	}
	
	@Test
	public void testDataSource() {
		//MysqlDataSource dataSource = new MysqlDataSource();
		SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
		dataSource.setUrl("jdbc:mysql://172.10.3.165:3306/?useUnicode=true&amp;characterEncoding=utf-8");
		dataSource.setUsername("root");
		dataSource.setPassword("!QAZxcvfr432");
		dataSource.setSuppressClose(true);
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		List<Map<String,Object>> maps = jdbcTemplate.queryForList("show databases");
		for(Map<String,Object> map : maps){
			Set<String> keySet = map.keySet();
			for(String key : keySet){
				System.out.println(key + ":" + map.get(key));
			}
		}
		try {
			Connection conn = jdbcTemplate.getDataSource().getConnection();
			System.out.println(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		jdbcTemplate.execute("use schedule");
		try {
			Connection conn = jdbcTemplate.getDataSource().getConnection();
			System.out.println(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		List<Map<String,Object>> list = jdbcTemplate.queryForList(" show tables ");
		for(Map<String,Object> map : list){
			Set<String> keySet = map.keySet();
			for(String key : keySet){
				System.out.println(key + ":" + map.get(key));
			}
		}
		
		try {
			Connection connection = jdbcTemplate.getDataSource().getConnection();
			boolean result = connection.isClosed();
			System.out.println(result);
			/*jdbcTemplate.getDataSource().getConnection().close();
			result = jdbcTemplate.getDataSource().getConnection().isClosed();
			System.out.println("----" + result);*/
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testTransaction(){
		SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
		dataSource.setUrl("jdbc:mysql://172.10.3.165:3306/?useUnicode=true&amp;characterEncoding=utf-8");
		dataSource.setUsername("root");
		dataSource.setPassword("!QAZxcvfr432");
		dataSource.setSuppressClose(true);
		DataSourceTransactionManager txManager = new DataSourceTransactionManager(dataSource);
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
	    def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
	    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
	    TransactionStatus status = txManager.getTransaction(def);
	    try {
	    	JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	    	jdbcTemplate.execute(" use schedule ");
	    	String insertSQL = 
	    	" INSERT INTO data_source  " + 
	        "    (id,                          " + 
	        "     master_url,                  " + 
	        "     slave_url,                   " + 
	        "     master_username,             " + 
	        "     MASTER_PASSWORD,             " + 
	        "     slave_username,              " + 
	        "     slave_password,              " + 
	        "     create_time,                 " + 
	        "     modify_time)                 " + 
			" VALUES (11,                       " + 
		    "    '192.168.1.1',                " + 
		    "    '192.168.2.1',                " + 
		    "    'user1',                      " + 
		    "    'password1',                  " + 
		    "    'user2',                      " + 
		    "    'password2',                  " + 
		    "    NOW(),                        " + 
		    "    NULL);                        " ;
	    	jdbcTemplate.execute(insertSQL);
	    	
	    	String regSQL =
		    	" INSERT INTO schedule.regulation  " +
	            "     (id,                         " +
	            "      source_id,                  " +
	            "      slave_url,                  " +
	            "      db_names,                   " +
	            "      table_names,                " +
	            "      create_time,                " +
	            "      modify_time)                " +
			    " VALUES                           " +
			    " 	(11,                            " +
		        "     11,                           " +
		        "     '192.168.2.1',               " +
		        "     'sttrade',                   " +
		        "     'owner_order',               " +
		        "     NOW(),                       " +
		        "     NOW())                       ";
	    	jdbcTemplate.execute(regSQL);
	    	
	    	String detailSQL = 
			" INSERT INTO regulation_detail  " + 
		    "             (id,               " + 
		    "              regulation_id,    " + 
		    "              col_name,         " + 
		    "              col_value,        " + 
		    "              col_type)         " + 
		    " VALUES (11,                     " + 
		    "         11,                   " + 
		    "         'order_no',            " + 
		    "         '1',                   " + 
		    "         'varchar');	         " ;
	    	jdbcTemplate.execute(detailSQL);		
	        txManager.commit(status);
	    } catch (RuntimeException e) {
	    	e.printStackTrace();
	        txManager.rollback(status);
	    }
	}
	
	@Test
	public void testDelete(){
			JdbcDaoFactory factory = JdbcDaoFactory.build(url,username,password);
			factory.getJdbcTemplate().execute("use schedule");
	    	String [] array = {
	    			" delete from data_source where id = 11 ",
	    			" delete from regulation where id = 11 "} ;
	    	
			factory.openTransaction();
			factory.setAutoCommit(true); 
			factory.getJdbcTemplate().batchUpdate(array);
			factory.getJdbcTemplate().execute(" delete from regulation_detail where id = 11 ");
			//factory.commit();
			List<Map<String,Object>> list = factory.getJdbcTemplate().queryForList("select * from data_source");
			for(Map<String,Object> map : list){
				System.out.println();
				for(String key : map.keySet()){
					System.out.print(key + ":" + map.get(key) + ",  ");
				}
			}
			factory.close();
	}
	
	@Test
	public void testAddBatch(){
		String [] array = new String[]{
				    	" INSERT INTO data_source  " + 
				        "    (id,                          " + 
				        "     master_url,                  " + 
				        "     slave_url,                   " + 
				        "     master_username,             " + 
				        "     MASTER_PASSWORD,             " + 
				        "     slave_username,              " + 
				        "     slave_password,              " + 
				        "     create_time,                 " + 
				        "     modify_time)                 " + 
						" VALUES (11,                       " + 
					    "    '192.168.1.1',                " + 
					    "    '192.168.2.1',                " + 
					    "    'user1',                      " + 
					    "    'password1',                  " + 
					    "    'user2',                      " + 
					    "    'password2',                  " + 
					    "    NOW(),                        " + 
					    "    NULL);                        " ,
					    	" INSERT INTO schedule.regulation  " +
				            "     (id,                         " +
				            "      source_id,                  " +
				            "      slave_url,                  " +
				            "      db_name,                   " +
				            "      table_name,                " +
				            "      create_time,                " +
				            "      modify_time)                " +
						    " VALUES                           " +
						    " 	(11,                            " +
					        "     11,                           " +
					        "     '192.168.2.1',               " +
					        "     'sttrade',                   " +
					        "     'owner_order',               " +
					        "     NOW(),                       " +
					        "     NOW())                       ",
				    	
						" INSERT INTO regulation_detail  " + 
					    "             (id,               " + 
					    "              regulation_id,    " + 
					    "              col_name,         " + 
					    "              col_value,        " + 
					    "              col_type)         " + 
					    " VALUES (11,                     " + 
					    "         11,                   " + 
					    "         'order_no',            " + 
					    "         '1',                   " + 
					    "         'varchar');	         " 
		};
		JdbcDaoFactory factory = JdbcDaoFactory.build(url,username,password);
		factory.openTransaction();
		factory.getJdbcTemplate().execute("use schedule");
		factory.getJdbcTemplate().batchUpdate(array);
		factory.commit();
		List<Map<String,Object>> list = factory.getJdbcTemplate().queryForList("select * from data_source");
		for(Map<String,Object> map : list){
			System.out.println();
			for(String key : map.keySet()){
				System.out.print(key + ":" + map.get(key) + ",  ");
			}
		}
		factory.close();
		
	}
}
