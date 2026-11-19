package com.ThreadPool.M_18_ThreadPooling;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
        log.warn("Task Rejected....");
        log.warn("Retrying After 1 second");
        try {
            // Delays the submitting thread before attempting to place the rejected task again.
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // A still-saturated executor can reject this submission and invoke this handler again.
        threadPoolExecutor.submit(runnable);
    }
}
