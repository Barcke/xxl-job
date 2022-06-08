package com.xxl.job.executor;

import com.xxl.job.core.handler.annotation.EnableSyncJob;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author xuxueli 2018-10-28 00:38:13
 */
@SpringBootApplication
@EnableSyncJob(userName = "admin",passWord = "123456",alarmEmail = "1225779679@qq.com",author = "barcke")
public class XxlJobExecutorApplication {

	public static void main(String[] args) {
        SpringApplication.run(XxlJobExecutorApplication.class, args);
	}

}
