package com.ThreadPool.M_18_ThreadPooling;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PaymentService {

    // Returns a future immediately while the payment lookup runs on the configured task executor.
    @Async
    public CompletableFuture<String> getPaymentInfo() throws InterruptedException {
        // Recording the current thread shows that this method is running on an executor worker.
        log.info("Fetching Payment Details.... using Thread:{}", Thread.currentThread().getName());
        // Simulates a four-second payment lookup without introducing an external dependency.
        Thread.sleep(4000);
        return CompletableFuture.completedFuture("Payment Id: 123");
    }

}
