package com.shankephone.data.cleaning.common;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 数据库操作类
 * 包括事务的开启、提交、回滚及连接的关闭
 * @author fengql
 * @version 2018年3月29日 下午4:04:15
 */
public class JdbcDaoFactory {
	
	public static final String URL_PREFIX = "jdbc:mysql://";
	public static final String URL_SUFFIX = "?useUnicode=true&amp;characterEncoding=utf-8";
	
	private SingleConnectionDataSource dataSource;
	private DataSourceTransactionManager txManager;
	private DefaultTransactionDefinition def;
	private TransactionStatus status;
	private JdbcTemplate jdbcTemplate;
	private Connection connection;
	private String dbname;
	
	/**
	 * 构造函数
	 * @param url
	 * @param username
	 * @param password
	 * @param dbname
	 */
	private JdbcDaoFactory(String url, String username, String password, String dbname){
		this(url, username, password);
		dataSource.setSchema(dbname);
	}
	
	/**
	 * 构造函数
	 * @param url
	 * @param username
	 * @param password
	 */
	private JdbcDaoFactory(String url, String username, String password){
		dataSource = new SingleConnectionDataSource();
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setSuppressClose(false);
		txManager = new DataSourceTransactionManager(dataSource);
		def = new DefaultTransactionDefinition();
		def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * 构建数据库操作实例
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 */
	public static JdbcDaoFactory build(String url, String username, String password) {
		return new JdbcDaoFactory(url, username, password);
	}
	
	/**
	 * 构建数据库操作实例，指定数据库
	 * @param url
	 * @param username
	 * @param password
	 * @param dbname
	 * @return
	 */
	public static JdbcDaoFactory build(String url, String username, String password, String dbname) {
		return new JdbcDaoFactory(url, username, password, dbname);
	}

	public JdbcTemplate getJdbcTemplate(){
		return jdbcTemplate;
	}
	
	public String getDbname(){
		return dbname;
	}
	
	/**
	 * 开启事务
	 */
	public void openTransaction() {
		status = txManager.getTransaction(def);
	}
	
	/**
	 * 设置提交方式
	 * @param commit
	 */
	public void setAutoCommit(boolean commit){
		try {
			connection.setAutoCommit(commit);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * 事务提交
	 */
	public void commit(){
		txManager.commit(status);
	}
	
	/**
	 * 事务回滚
	 */
	public void rollback(){
		txManager.rollback(status);
	}
	
	/**
	 * 关闭连接
	 */
	public void close(){
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(connection != null){
					connection.close();
				}
			} catch (SQLException e) {
				connection = null;
			}
		}
	}

}
