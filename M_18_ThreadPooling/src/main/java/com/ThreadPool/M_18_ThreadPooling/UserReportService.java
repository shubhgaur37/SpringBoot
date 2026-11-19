package com.ThreadPool.M_18_ThreadPooling;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
public class UserReportService {

    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public UserReportService(UserService userService, OrderService orderService, PaymentService paymentService) {
        this.userService = userService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    public CompletableFuture<List<String>> fetchUserReport() throws InterruptedException {

        // This method submits work on the request thread but returns without awaiting the results.
        log.info("Fetching User Report using Thread: {}",
                Thread.currentThread().getName());

        long start = System.currentTimeMillis();

        // Each @Async call submits an independent task and returns its pending result.
        // Saturated submissions can pause here while the rejection handler retries.
        CompletableFuture<String> userFuture = userService.getUserInfo();
        CompletableFuture<String> orderFuture = orderService.getOrderInfo();
        CompletableFuture<String> paymentFuture = paymentService.getPaymentInfo();

        // allOf() represents completion of every task without blocking this thread.
        // thenApply() assembles the report only after successful aggregate completion.
        return CompletableFuture.allOf(userFuture, orderFuture, paymentFuture)
                .thenApply(ignoredParam -> {

                    // These joins retrieve already-completed results in response order.
                    List<String> userReport = List.of(
                            userFuture.join(),
                            orderFuture.join(),
                            paymentFuture.join()
                    );

                    // Records the thread that executes this non-async continuation.
                    log.info("User Report Fetch Completed using Thread: {}",
                            Thread.currentThread().getName());

                    // Covers submission, queueing, service execution, and report assembly.
                    log.info("Total Time taken to generate Report: {}",
                            System.currentTimeMillis() - start);

                    return userReport;
                })
                .whenComplete((result, exception) -> {

                    // Logs exceptional completion without recovering or replacing it.
                    if (exception != null) {
                        log.error("User Report generation failed", exception);
                    }
                });
    }
}
