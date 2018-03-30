package com.shankephone.data.cleaning.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.shankephone.data.cleaning.common.JdbcDaoFactory;
import com.shankephone.data.cleaning.model.DBDataSource;
import com.shankephone.data.cleaning.model.QueryInfo;
import com.shankephone.data.cleaning.model.Regulation;
import com.shankephone.data.cleaning.model.RegulationDetail;

/**
 * 数据清理处理类
 * @author fengql
 * @version 2018年3月29日 下午7:27:40
 */
public class DataCleaningJob {
	
	private Properties properties = new Properties();
	
	//规则配置数据源
	private String url;
	private String username;
	private String password;
	private MysqlDataSource dataSource = new MysqlDataSource();
	private JdbcDaoFactory regFactory;
	
	//历史库数据源
	private String historyUrl;
	private String historyUsername;
	private String historyPassword;
	private MysqlDataSource historyDataSource = new MysqlDataSource();
	private JdbcDaoFactory historyFactory;
	
	public static void main(String[] args) {
		DataCleaningJob job = new DataCleaningJob();
		job.execute();
	}
	
	public void execute(){
		Map<String,DBDataSource> dbsources = generateRegulation();
		List<QueryInfo> list = generateQueryInfo(dbsources);
		for(QueryInfo queryInfo : list){
			String slaveSQL = queryInfo.getSlaveSQL();
			JdbcTemplate slaveJdbc = queryInfo.getSlaveDao().getJdbcTemplate();
			List<Map<String,Object>> result = slaveJdbc.queryForList(slaveSQL);
			JdbcTemplate masterJdbc = queryInfo.getMasterDao().getJdbcTemplate();
			masterJdbc.execute(queryInfo.getMasterSQL());
			System.out.println(JSONObject.toJSON(result));
		}
	}
	
	public DataCleaningJob(){
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
			//规则配置数据源
			url = properties.getProperty("dataSource.url");
			username = properties.getProperty("dataSource.username");
			password = properties.getProperty("dataSource.password");
			dataSource.setUrl(url);
			dataSource.setUser(username);
			dataSource.setPassword(password);
			regFactory = JdbcDaoFactory.build(url, username, password);
			
			//历史库数据源
			historyUrl = properties.getProperty("history.dataSource.url");
			historyUsername = properties.getProperty("history.dataSource.username");
			historyPassword = properties.getProperty("history.dataSource.password");
			historyDataSource.setUrl(historyUrl);
			historyDataSource.setUser(historyUsername);
			historyDataSource.setPassword(historyPassword);
			historyFactory = JdbcDaoFactory.build(historyUrl, historyUsername, historyPassword);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 生成规则关联
	 * @return
	 */
	public Map<String,DBDataSource> generateRegulation(){
		Map<String,DBDataSource> dsMap = new HashMap<String,DBDataSource>();
		//查询规则定义的所有数据源列表
		List<Map<String,Object>> dataSources = regFactory.getJdbcTemplate().queryForList(" select * from data_source ");
		for(Map<String,Object> record : dataSources){
			DBDataSource dbDatasource = new DBDataSource();
			String id = String.valueOf(record.get("id"));
			
			String masterUrl = String.valueOf(record.get("master_url"));
			String masterUsername = String.valueOf(record.get("master_username"));
			String masterPassword = String.valueOf(record.get("master_password"));
			
			String slaveUrl = String.valueOf(record.get("slave_url"));
			String slaveUsername = String.valueOf(record.get("slave_username"));
			String slavePassword = String.valueOf(record.get("slave_password"));
			
			dbDatasource.setId(id);
			dbDatasource.setMasterUrl(masterUrl);
			dbDatasource.setMasterUsername(masterUsername);
			dbDatasource.setMasterPassword(masterPassword);
			dbDatasource.setSlaveUrl(slaveUrl);
			dbDatasource.setSlaveUsername(slaveUsername);
			dbDatasource.setSlavePassword(slavePassword);
			
			List<Map<String,Object>> regulations = regFactory.getJdbcTemplate().queryForList(" select * from regulation where source_id = " + id);
			for(Map<String,Object> reg : regulations){
				String regId = String.valueOf(reg.get("id"));
				String sourceId = String.valueOf(reg.get("source_id"));
				String dbName = String.valueOf(reg.get("db_name"));
				String tableName = String.valueOf(reg.get("table_name"));
				Date createTime = (Date)reg.get("create_time");
				Date modifyTime = (Date)reg.get("modify_time");
				
				Regulation regulation = new Regulation();
				regulation.setId(regId);
				regulation.setSourceId(sourceId);
				regulation.setDbName(dbName);
				regulation.setTableName(tableName);
				regulation.setCreateTime(createTime);
				regulation.setModifyTime(modifyTime);
				
				List<Map<String,Object>> details = regFactory.getJdbcTemplate().queryForList(" select * from regulation_detail where regulation_id = " + id);
				for(Map<String,Object> detail : details){
					String detailId = String.valueOf(detail.get("id"));
					String regulationId = String.valueOf(detail.get("regulation_id"));
					String colName = String.valueOf(detail.get("col_name"));
					String colValue = String.valueOf(detail.get("col_value"));
					String colType = String.valueOf(detail.get("col_type"));
					String operator = String.valueOf(detail.get("operator"));
					RegulationDetail dt = new RegulationDetail();
					dt.setId(detailId);
					dt.setRegulationId(regulationId);
					dt.setColName(colName);
					dt.setColValue(colValue);
					dt.setColType(colType);
					dt.setOperator(operator);
					regulation.getDetails().add(dt);
				}
				dbDatasource.getRegulations().add(regulation);
				
			}
			dsMap.put(id,dbDatasource);
		}
		return dsMap;
	}
	
	/**
	 * 生成执行的SQL语句
	 * @param dbsources
	 * @return
	 */
	public List<QueryInfo> generateQueryInfo(Map<String,DBDataSource> dbsources) {
		List<QueryInfo> queryInfos = new ArrayList<QueryInfo>();
		for(String sourceId : dbsources.keySet()){
			DBDataSource source = dbsources.get(sourceId);
			List<Regulation> regs = source.getRegulations();
			if(regs.size() > 0){
				for(Regulation reg : source.getRegulations()){
					//创建从库查询信息
					QueryInfo queryInfo = new QueryInfo();
					queryInfo.setId(sourceId);
					queryInfo.setDbName(reg.getDbName());
					//从库配置
					String slaveUrl = source.getSlaveUrl();
					slaveUrl = JdbcDaoFactory.URL_PREFIX + slaveUrl + "/" + queryInfo.getDbName() + JdbcDaoFactory.URL_SUFFIX;
					queryInfo.setSlaveUrl(slaveUrl);
					queryInfo.setSlaveUsername(source.getSlaveUsername());
					queryInfo.setSlavePassword(source.getSlavePassword());
					
					//主库配置
					String masterUrl = source.getMasterUrl();
					masterUrl = JdbcDaoFactory.URL_PREFIX + masterUrl + "/" + queryInfo.getDbName() + JdbcDaoFactory.URL_SUFFIX;
					queryInfo.setMasterUrl(masterUrl);
					queryInfo.setMasterUsername(source.getMasterUsername());
					queryInfo.setMasterPassword(source.getMasterPassword());
					
					String dbName = reg.getDbName();
					String tableName = reg.getTableName();
					String slaveSQL = "select * from " + dbName + "." + tableName;
					String masterSQL = "delete from " + dbName + "." + tableName;
					List<RegulationDetail> details = reg.getDetails();
					if(details.size() > 0){
						String conditions = " where 1 = 1 ";
						for(RegulationDetail detail : details){
							String colType = detail.getColType();
							String colValue = detail.getColValue();
							switch(colType){
								case "varchar": 
									colValue = "'" + colValue + "'"; break;
								default : 
									break;
							}
							String term = " and " + detail.getColName() + " " + detail.getOperator() + " " + colValue; 
							conditions += term;
						}
						slaveSQL += conditions;
						masterSQL += conditions;
					}
					queryInfo.setSlaveSQL(slaveSQL); 
					queryInfo.setMasterSQL(masterSQL);
					JdbcDaoFactory masterDao = JdbcDaoFactory.build(masterUrl, queryInfo.getMasterUsername(), queryInfo.getMasterPassword());
					JdbcDaoFactory slaveDao = JdbcDaoFactory.build(slaveUrl, queryInfo.getSlaveUsername(), queryInfo.getSlavePassword());
					String json = JSONObject.toJSON(queryInfo).toString();
					System.out.println(json);
					queryInfo.setMasterDao(masterDao);
					queryInfo.setSlaveDao(slaveDao);
					queryInfos.add(queryInfo);
				}
			}
		}
		return queryInfos;
	}

}
