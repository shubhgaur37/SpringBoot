package com.ThreadPool.M_18_ThreadPooling;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class OrderService {
    // Returns a future immediately while the order lookup runs on the configured task executor.
    @Async
    public CompletableFuture<String> getOrderInfo() throws InterruptedException {
        // Recording the current thread shows that this method is running on an executor worker.
        log.info("Fetching Order Details.... using Thread:{}", Thread.currentThread().getName());
        // Simulates a three-second order lookup without introducing an external dependency.
        Thread.sleep(3000);
        return CompletableFuture.completedFuture("Order Id: dfdsdv1342");
    }
}
