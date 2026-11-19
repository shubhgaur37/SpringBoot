package com.ThreadPool.M_18_ThreadPooling;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<String> fetchUserReport() {
        // This method stays on the request thread while the delegated service methods run asynchronously.
        log.info("Fetching User Report using Thread: {}", Thread.currentThread().getName());
        List<String> userReport = new ArrayList<>();
        Long start = System.currentTimeMillis();
        try {
            // Submit all lookups before waiting so the executor can overlap their work.
            CompletableFuture<String> userFuture = userService.getUserInfo();
            CompletableFuture<String> orderFuture = orderService.getOrderInfo();
            CompletableFuture<String> paymentFuture = paymentService.getPaymentInfo();
            // The aggregate future is created but not awaited; the get() calls below perform the waiting.
            CompletableFuture.allOf(userFuture,orderFuture,paymentFuture);

            // Each get() blocks if needed and preserves user, order, payment order in the report.
            userReport.add(userFuture.get());
            userReport.add(orderFuture.get());
            userReport.add(paymentFuture.get());
            Long end = System.currentTimeMillis();
            log.info("User Report Fetch Completed using Thread: {}", Thread.currentThread().getName());
            // Reports the elapsed time for submitting, waiting for, and collecting all results.
            log.info("Total Time taken to generate Report: {}", end - start);
        }
        catch (Exception e){
            log.error("Exception occured: {}", e.getMessage());


        }

        return userReport;

    }
}

