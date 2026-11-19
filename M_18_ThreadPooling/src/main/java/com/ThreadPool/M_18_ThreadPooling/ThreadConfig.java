package com.ThreadPool.M_18_ThreadPooling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadConfig {

    // Spring uses this executor for the @Async service methods.
    @Bean
    ThreadPoolTaskExecutor getThreadPoolTaskExecutor(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // Keeps two worker threads available for asynchronous tasks.
        taskExecutor.setCorePoolSize(2);
        // Caps the pool at five workers when the work queue cannot accept more tasks.
        taskExecutor.setMaxPoolSize(5);
        // Allows non-core workers to stop after five idle seconds.
        taskExecutor.setKeepAliveSeconds(5);
        // Makes executor threads identifiable in the service log messages.
        taskExecutor.setThreadNamePrefix("TestParallel_");
        return taskExecutor;
    }
}
