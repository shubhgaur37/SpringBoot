package com.ThreadPool.M_18_ThreadPooling;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserReportController {

    private final UserReportService userReportService;

    public UserReportController(UserReportService userReportService) {
        this.userReportService = userReportService;
    }

    @GetMapping("/users/reports")
    public List<String> getUserReport(){
        // The response is returned after the report service has collected all three results.
        return userReportService.fetchUserReport();
    }
}
