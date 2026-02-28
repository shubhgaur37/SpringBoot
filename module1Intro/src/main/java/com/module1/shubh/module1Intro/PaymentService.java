package com.module1.shubh.module1Intro;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

// Used to define bean to offload the responsibility of object creation and managing it to Spring Framework
// Now it can directly be used without manual initialisation, which would be taken care by the framework itself
@Component
//@Controller
public class PaymentService {
    public void pay(){
        System.out.println("Paying....");
    }

    @PostConstruct
    public void afterInit(){
//        called after all the beans are initialised and injected
        System.out.println("BEFORE PAYING");
    }
    @PreDestroy
    public void beforeDestroy(){
//        this method will be called when all the beans are destroyed. i.e. when the application stops running
        System.out.println("After Payment is Done");
    }
}
