package com.ThreadPool.M_18_ThreadPooling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;

@Configuration
public class ThreadConfig {

    // Spring uses this task executor for the @Async service methods.
    @Bean
    ThreadPoolTaskExecutor getThreadPoolTaskExecutor(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // Keeps two workers available before tasks are placed in the queue.
        taskExecutor.setCorePoolSize(2);
        // Allows one additional worker when the queue is full, up to three workers total.
        taskExecutor.setMaxPoolSize(3);
        // Retires an idle non-core worker after five seconds.
        taskExecutor.setKeepAliveSeconds(5);

        // Makes executor workers identifiable in the service log messages.
        taskExecutor.setThreadNamePrefix("TestParallel_");

        // Holds one task before the pool attempts to grow beyond its core size.
        taskExecutor.setQueueCapacity(1);
        // Delays and resubmits work rejected after both the pool and queue are full.
        taskExecutor.setRejectedExecutionHandler(new CustomRejectedExecutionHandler());
        return taskExecutor;
    }
}
