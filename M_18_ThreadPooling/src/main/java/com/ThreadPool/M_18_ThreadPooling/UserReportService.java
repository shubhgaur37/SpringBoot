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

        // This method initially executes on the Tomcat request thread.
        // The method returns a CompletableFuture instead of waiting
        // synchronously for the report to be generated.
        log.info("Fetching User Report using Thread: {}",
                Thread.currentThread().getName());

        long start = System.currentTimeMillis();

        // Submit all three independent tasks to the async executor.
        // Each @Async service method returns a CompletableFuture<String>.
        //
        // The tasks can execute concurrently, subject to executor capacity.
        // Task submission itself may block if the rejection handler waits
        // for capacity, or fail if the executor rejects the task.
        CompletableFuture<String> userFuture = userService.getUserInfo();
        CompletableFuture<String> orderFuture = orderService.getOrderInfo();
        CompletableFuture<String> paymentFuture = paymentService.getPaymentInfo();

        // allOf() creates a combined CompletableFuture<Void>.
        // It completes successfully only when all three futures succeed.
        //
        // IMPORTANT:
        // allOf() is non-blocking. Unlike allOf().join(), it does not
        // make the calling thread wait for task completion.
        //
        // thenApply() registers a continuation that executes only after
        // the combined future completes successfully.
        return CompletableFuture.allOf(userFuture, orderFuture, paymentFuture)
                .thenApply(ignoredParam -> {

                    // All three futures have completed successfully.
                    //
                    // CompletableFuture.join() retrieves each result.
                    // It does not wait again here because the futures
                    // are already complete.
                    //
                    // This is NOT Thread.join(), which waits for a
                    // thread to terminate.
                    List<String> userReport = List.of(
                            userFuture.join(),
                            orderFuture.join(),
                            paymentFuture.join()
                    );

                    // thenApply() is not guaranteed to execute on the
                    // original Tomcat thread. It generally executes on
                    // the thread completing the combined future, or on
                    // the registering thread if already completed.
                    log.info("User Report Fetch Completed using Thread: {}",
                            Thread.currentThread().getName());

                    // Measures the time from the start of this method
                    // until the report has been assembled.
                    // This includes submission delays, executor queueing,
                    // task execution and result collection.
                    // It does not measure HTTP response delivery time.
                    log.info("Total Time taken to generate Report: {}",
                            System.currentTimeMillis() - start);

                    return userReport;
                })
                .whenComplete((result, exception) -> {

                    // Observe asynchronous failures without converting
                    // them into successful responses.
                    //
                    // If any service future fails, allOf() completes
                    // exceptionally and thenApply() is skipped.
                    // The exception remains available to Spring MVC.
                    if (exception != null) {
                        log.error("User Report generation failed", exception);
                    }
                });
    }
}
