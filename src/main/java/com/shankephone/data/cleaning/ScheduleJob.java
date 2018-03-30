package com.shankephone.data.cleaning;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;

/**
 * 数据清理任务
 * @author fengql
 * @version 2018年3月28日 下午8:07:19
 */
public class ScheduleJob implements SimpleJob {

	public void execute(ShardingContext shardingContext) {
		String parameters = shardingContext.getJobParameter();
		
	}
    
   
}