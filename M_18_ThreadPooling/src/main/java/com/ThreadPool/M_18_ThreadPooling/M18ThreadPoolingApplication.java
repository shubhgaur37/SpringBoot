package com.ThreadPool.M_18_ThreadPooling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables Spring's asynchronous processing so @Async service calls use the configured task executor.
 */
@SpringBootApplication
@EnableAsync
public class M18ThreadPoolingApplication {
    public static void main(String[] args) {
        SpringApplication.run(M18ThreadPoolingApplication.class, args);
    }

}
