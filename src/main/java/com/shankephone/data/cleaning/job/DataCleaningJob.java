package com.shankephone.data.cleaning.job;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.shankephone.data.cleaning.common.JdbcDaoFactory;

/**
 * 数据清理处理类
 * @author fengql
 * @version 2018年3月29日 下午7:27:40
 */
@Slf4j
public class DataCleaningJob {
	
	private Properties properties = new Properties();
	private String jobName="";//作业名称
	
	//规则配置数据源
	private String url;
	private String username;
	private String password;
	private MysqlDataSource dataSource = new MysqlDataSource();
	private JdbcDaoFactory regFactory;
	
	public DataCleaningJob(String jobName){
		this.jobName=jobName;
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
			//规则配置数据源
			url = properties.getProperty("dataSource.url");
			username = properties.getProperty("dataSource.username");
			password = properties.getProperty("dataSource.password");
			dataSource.setUrl(url);
			dataSource.setUser(username);
			dataSource.setPassword(password);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void execute(){
		regFactory = JdbcDaoFactory.build(url, username, password);
		String dataSourceSQL = " SELECT r.* FROM job_reg_relation r LEFT JOIN job_setting j ON r.job_id=j.id WHERE j.job_name='" + jobName + "'";
    	List<Map<String,Object>> maps = regFactory.getJdbcTemplate().queryForList(dataSourceSQL);
    	if(maps!=null && maps.size()>0){
    		for(int i=0;i<maps.size();i++){
    			Map<String,Object> map=maps.get(i);
    			long reg_id=(Long)map.get("reg_id");//规则id
    			generateRegulation(reg_id);
    		}
    	}
	}
	 
	/**
	 * 生成规则关联
	 * @return
	 */
	public void generateRegulation(long reg_id){
		Map<String,Object> regulationMap = regFactory.getJdbcTemplate().queryForMap(" select * from regulation where id = " + reg_id );
		long source_id=(Long)regulationMap.get("source_id");
		long history_source_id=(Long)regulationMap.get("history_source_id");
		String db_name=(String)regulationMap.get("db_name");//源库名
		String history_db_name=(String)regulationMap.get("history_db_name");//历史库名
		String sql_txt=(String)regulationMap.get("sql_txt");//清理数据的SQL语句
		String check_sql=(String)regulationMap.get("check_sql");//校验从库SQL
		String check_history_sql=(String)regulationMap.get("check_history_sql");//校验历史库SQL
		
		Map<String,Object> dataSourceMap = regFactory.getJdbcTemplate().queryForMap(" select * from data_source where id="+source_id);
		Map<String,Object> historyDataSourceMap = regFactory.getJdbcTemplate().queryForMap(" select * from data_source where id="+history_source_id);
		regFactory.close();
		//目标数据源（主）
		String master_url=JdbcDaoFactory.URL_PREFIX + (String)dataSourceMap.get("master_url") + "/"+ db_name + JdbcDaoFactory.URL_SUFFIX;
		String master_username=(String)dataSourceMap.get("master_username");
		String master_password=(String)dataSourceMap.get("master_password");
		JdbcDaoFactory masterFactory = JdbcDaoFactory.build(master_url, master_username, master_password);
		//目标数据源（从）
		String slave_url=JdbcDaoFactory.URL_PREFIX + (String)dataSourceMap.get("slave_url") + "/"+ db_name + JdbcDaoFactory.URL_SUFFIX;
		String slave_username=(String)dataSourceMap.get("slave_username");
		String slave_password=(String)dataSourceMap.get("slave_password");
		JdbcDaoFactory slaveFactory = JdbcDaoFactory.build(slave_url, slave_username, slave_password);
		//历史数据源
		String historyMaster_url=JdbcDaoFactory.URL_PREFIX + (String)historyDataSourceMap.get("master_url") + "/"+ history_db_name + JdbcDaoFactory.URL_SUFFIX;
		String historyMaster_username=(String)historyDataSourceMap.get("master_username");
		String historyMaster_password=(String)historyDataSourceMap.get("master_password");
		JdbcDaoFactory historyFactory = JdbcDaoFactory.build(historyMaster_url, historyMaster_username, historyMaster_password);
		
		//一致性校验
		Map<String,Object> checkMap = new HashMap<String,Object>();
		boolean isSlaveExist=false;//从库是否存在
		if("".equals(slave_url) || "".equals(slave_username) || "".equals(slave_password) ){
			checkMap = masterFactory.getJdbcTemplate().queryForMap(check_sql);
		}
		else{
			isSlaveExist=true;
			checkMap = slaveFactory.getJdbcTemplate().queryForMap(check_sql);
		}
		Map<String,Object> historyMap=historyFactory.getJdbcTemplate().queryForMap(check_history_sql);
		historyFactory.close();
		if(checkMap!=null && historyMap!=null && checkMap.size()==historyMap.size()){
			long checkCount = (Long)checkMap.get("count");
			long historyCount = (Long)historyMap.get("count");
			log.info("history:  "+ historyCount);
			log.info("checkDB:  "+checkCount);
			if(historyCount == checkCount) {
				log.info("masterSQL:    " + sql_txt); 
				String binlogSetSqlNo="set sql_log_bin=0;";
				String binlogSetSqlYes="set sql_log_bin=1;";
				try {
					masterFactory.openTransaction();
					masterFactory.setAutoCommit(false);
					JdbcTemplate masterJdbc = masterFactory.getJdbcTemplate();
					masterJdbc.execute(binlogSetSqlNo);	//设置binlog日志关闭
					masterJdbc.execute(sql_txt);	//主库删除
					masterFactory.commit();
					masterJdbc.execute(binlogSetSqlYes);	//设置binlog日志开启
					if(isSlaveExist){
						slaveFactory.openTransaction();
						slaveFactory.setAutoCommit(false);
						JdbcTemplate slaveJdbc = slaveFactory.getJdbcTemplate();
						slaveJdbc.execute(binlogSetSqlNo);//设置binlog日志关闭
						slaveJdbc.execute(sql_txt);//从库删除
						slaveFactory.commit();
						slaveJdbc.execute(binlogSetSqlYes);	//设置binlog日志开启
						log.info("------------------删除数据记录数：" + checkCount);
					}
				} catch (DataAccessException e) {
					masterFactory.rollback();
					log.info("------------------删除数据记录异常：已撤消！");
				} finally {
					masterFactory.close();
					if(isSlaveExist){
						slaveFactory.close();
					}
					log.info("------------------连接已关闭！");
				}
			} else{
				log.info("未通过校验！");
			}
		}
		
	}

}
