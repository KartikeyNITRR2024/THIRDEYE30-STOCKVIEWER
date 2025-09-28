package com.thirdeye3.stockviewer.configs;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class WebscrapperAsyncConfig {
	@Bean(name="WebscrapperAsynchThread")
	public Executor getThreadPoolExecutor()
	{
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(30);
		executor.setMaxPoolSize(100);
		executor.setThreadNamePrefix("Webscrapper-");
		executor.initialize();
		return executor;
	}
}