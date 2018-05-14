package com.shankephone.data.cleaning;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.dao.DataAccessException;

import com.alibaba.fastjson.JSONObject;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.shankephone.data.cleaning.common.JdbcDaoFactory;
import com.shankephone.data.cleaning.common.PropertiesUtils;

/**
 * 构建任务类
 * @author fengql
 * @version 2018年3月28日 下午8:06:12
 */
@Slf4j
public class Executor {
	
    public static void main(String[] args) {
    	if(args == null || args.length < 1){
    		throw new RuntimeException("arguments is null !");
    	}
    	String jobName = args[0];
    	Executor executor = new Executor();
    	if(args!=null && args.length>0){
    		executor.start(jobName);
    	}
    }
    
    /**
     * 启动作业执行环境
     * @param args
     */
    public void start(String jobName){
    	String className  = SimpleJobExecutor.class.getCanonicalName();
    	JobScheduler job = Executor.createJob(className,jobName);
    	job.init();
    }
    
    public static String getClassName(String fullname){
    	return fullname.substring(fullname.lastIndexOf(".") + 1);
    }
    /**
     * 
     * @param className ：要执行的job类
     * @param jobSettingId ： 作业id
     * @return
     */
    public static JobScheduler createJob(String className,String jobName){
    	JdbcDaoFactory jdbcDao = JdbcDaoFactory.build(PropertiesUtils.load());
    	
    	//查询注册中心
    	String sql = "select * from job_registry_center where activated = 1";
    	Map<String,Object> map = jdbcDao.getJdbcTemplate().queryForMap(sql);
    	String namespace = String.valueOf(map.get("namespace"));
    	String zklist = String.valueOf(map.get("zklist"));
    	CoordinatorRegistryCenter registryCenter = createRegistryCenter(zklist, namespace);
    	
    	//查询任务配置
    	String jobSQL = "select * from job_setting where job_name = '" + jobName + "'";
    	Map<String,Object> jobMap = null;
    	try {
			jobMap = jdbcDao.getJdbcTemplate().queryForMap(jobSQL);
		} catch (DataAccessException e) {
			log.warn("没有作业配置，将自动创建作业配置！"); 
		}
    	LiteJobConfiguration jobConf = null;
    	if(jobMap == null || jobMap.size() == 0){
    		log.warn("没有查询到作业配置！"); 
    		return null;
    	} 
    	
    	JobEventConfiguration jobEventRdbConfig = null;
    	
    	String dataSourceSQL = " select * from job_event_trace_source where activated = 1 ";
    	List<Map<String,Object>> maps = jdbcDao.getJdbcTemplate().queryForList(dataSourceSQL);
    	if(maps != null && maps.size() > 0){
    		Map<String,Object> m = maps.get(0);
    		String url = String.valueOf(m.get("url"));
    		String username = String.valueOf(m.get("username"));
    		String password = String.valueOf(m.get("password"));
    		String driver = String.valueOf(m.get("driver"));
    		// 初始化数据源
    		BasicDataSource dataSource = new BasicDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setDriverClassName(driver);
            // 定义日志数据库事件溯源配置
            jobEventRdbConfig = new JobEventRdbConfiguration(dataSource);
    	}
    	String during = String.valueOf(jobMap.get("cron"));
		String shardingNum = String.valueOf(jobMap.get("sharding_total_count")==null?"":jobMap.get("sharding_total_count"));//分片
		shardingNum="".equals(shardingNum)?"1":shardingNum;
		Object status = map.get("status");
		if(status == null){
			String updateSQL = "update job_setting set status = 1 where id = " + String.valueOf(jobMap.get("id"));
    		jdbcDao.getJdbcTemplate().execute(updateSQL);
		}
		jobConf = createJobConfiguration(className, during, Integer.parseInt(shardingNum),jobName);
        jdbcDao.close();
        if(jobEventRdbConfig != null){
        	JobScheduler scheduler =  new JobScheduler(registryCenter, jobConf, jobEventRdbConfig); 
        	log.info("创建事件跟踪库成功");
        	return scheduler;
        }
        log.warn("创建事件跟踪库失败！不能获取事件跟踪");
    	return new JobScheduler(registryCenter, jobConf);
    }
    
    /**
     * 初始化注册中心
     * @param zkList    zookeeper地址
     * @param namespace    zookeeper中的命名空间
     * @return
     */
    private static CoordinatorRegistryCenter createRegistryCenter(String zkList, String namespace) {
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(new ZookeeperConfiguration(zkList, namespace));
        regCenter.init();
        return regCenter;
    }
    
    /**
     * 创建作业配置
     * @param className  执行作业的类名，来自于命令参数，作业名jobName截取自该参数className
     * @param during  定时规则
     * @param shardingCount  分片数，默认为1， 即不分片
     * @return
     */
    private static LiteJobConfiguration createJobConfiguration(String className, String during, int shardingCount,String jobName) {
    	JSONObject json = new JSONObject();
    	json.put("jobName", jobName);
    	// 定义作业核心配置
        JobCoreConfiguration simpleCoreConfig = JobCoreConfiguration.newBuilder(jobName, during, shardingCount).jobParameter(json.toJSONString()).build();
        // 定义SIMPLE类型配置
        SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(simpleCoreConfig, className);
        // 定义Lite作业根配置
        LiteJobConfiguration simpleJobRootConfig = LiteJobConfiguration.newBuilder(simpleJobConfig).overwrite(true).build();
        return simpleJobRootConfig;
    }
}
