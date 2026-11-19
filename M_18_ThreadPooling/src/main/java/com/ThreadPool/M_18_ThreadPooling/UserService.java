package com.ThreadPool.M_18_ThreadPooling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class UserService {

    // Returns a future immediately while the user lookup runs on the configured task executor.
    @Async
    public CompletableFuture<String> getUserInfo() throws InterruptedException {
        // Recording the current thread shows that this method is running on an executor worker.
        log.info("Fetching User Details.... using Thread:{}", Thread.currentThread().getName());
        // Simulates a two-second user lookup without introducing an external dependency.
        Thread.sleep(2000);
        return CompletableFuture.completedFuture("User Id: 01t435");
    }
}
