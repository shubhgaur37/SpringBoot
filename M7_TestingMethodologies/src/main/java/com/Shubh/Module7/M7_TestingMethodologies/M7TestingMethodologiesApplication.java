package com.Shubh.Module7.M7_TestingMethodologies;

import com.Shubh.Module7.M7_TestingMethodologies.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@RequiredArgsConstructor
@SpringBootApplication

// CommandLineRunner executes after Spring Boot has fully started and the
// application context has been initialized. It is one of the last callbacks
// during application startup and is commonly used for one-time startup tasks
// such as loading seed data or performing initialization logic.
public class M7TestingMethodologiesApplication implements CommandLineRunner {

    // Multiple DataService beans exist, so Spring requires @Primary or
    // @Qualifier to determine which bean to inject.
    private final DataService dataService;

    @Value("${deployment.env}")
    private String deploymentEnv;

    public static void main(String[] args) {
        SpringApplication.run(M7TestingMethodologiesApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("The Environment is: " + dataService.getEnvironment());
        System.out.println("The Data Source is: " + dataService.getData());
    }
}