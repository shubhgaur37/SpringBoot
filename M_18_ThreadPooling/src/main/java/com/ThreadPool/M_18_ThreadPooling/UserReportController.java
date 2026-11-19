package com.ThreadPool.M_18_ThreadPooling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
public class UserReportController {

    private final UserReportService userReportService;

    public UserReportController(UserReportService userReportService) {
        this.userReportService = userReportService;
    }


    @GetMapping("/users/reports")
    public CompletableFuture<List<String>> getUserReport() throws InterruptedException {
        // Spring MVC recognizes CompletableFuture as an asynchronous
        // response type and starts asynchronous request processing.
        //
        // Once the future has been returned, the original Tomcat
        // worker can be released while the report is generated.
        CompletableFuture<List<String>> userReports = userReportService.fetchUserReport();
        return userReports;
    }

}
