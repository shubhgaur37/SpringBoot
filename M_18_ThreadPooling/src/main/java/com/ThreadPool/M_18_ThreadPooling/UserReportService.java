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

        // The method executes on the Tomcat request thread.
        log.info("Fetching User Report using Thread: {}", Thread.currentThread().getName());

        List<String> userReport = new ArrayList<>();
        Long start = System.currentTimeMillis();

        try {
            // Submit all three independent tasks to the async executor.
            // Each service method returns a CompletableFuture, allowing the tasks
            // to execute concurrently on separate executor threads.
            CompletableFuture<String> userFuture = userService.getUserInfo();
            CompletableFuture<String> orderFuture = orderService.getOrderInfo();
            CompletableFuture<String> paymentFuture = paymentService.getPaymentInfo();

            // Combine the three futures and block the Tomcat request thread
            // until all tasks have completed.
            // allOf() itself is non-blocking; join() performs the blocking wait.
            CompletableFuture.allOf(userFuture, orderFuture, paymentFuture).join();

            // Retrieve the completed results in a consistent order.
            // get() is normally a blocking operation when a future is incomplete,
            // but these futures have already completed because of allOf().join().
            // Therefore, these calls do not wait again under normal execution.
            userReport.add(userFuture.get());
            userReport.add(orderFuture.get());
            userReport.add(paymentFuture.get());

            Long end = System.currentTimeMillis();

            log.info("User Report Fetch Completed using Thread: {}", Thread.currentThread().getName());

            // Measure the total elapsed time, including task submission,
            // executor queueing, asynchronous execution, and result collection.
            log.info("Total Time taken to generate Report: {}", end - start);

        } catch (Exception e) {
            log.error("Exception occured: {}", e.getMessage());
        }

        return userReport;
    }
}

