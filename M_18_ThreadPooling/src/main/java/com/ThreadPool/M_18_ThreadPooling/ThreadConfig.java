package com.ThreadPool.M_18_ThreadPooling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

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

    // Provides a separate scheduler for delayed email tasks.
    @Bean
    TaskScheduler getTaskScheduler(){
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        // Allows up to three scheduled actions to execute at the same time when they become due.
        taskScheduler.setPoolSize(3);
        // Identifies scheduler workers in the email execution log.
        taskScheduler.setThreadNamePrefix("Task_Scheduler_");
        return taskScheduler;
    }
}
