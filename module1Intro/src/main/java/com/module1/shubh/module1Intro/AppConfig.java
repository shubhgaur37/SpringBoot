package com.module1.shubh.module1Intro;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

// signifies that configuration for the project reside here
// project beans and how they should be instantiated would be defined here
@Configuration
public class AppConfig {
// @Bean annotation takes priority over other ways of defining beans
    @Bean
    @Scope("prototype")
    public PaymentService paymentService() {
//        defines how the bean should be initialised
//        its a simple object with an empty constructor, but for complex objects we need to specify, the object creation logic
        return new PaymentService();
    }
}

