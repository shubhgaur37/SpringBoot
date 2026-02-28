package com.module1.shubh.module1Intro.concrete_notification_services;

import com.module1.shubh.module1Intro.NotificationService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;


@Component
@Primary // default bean to be injected using @Autowired in case of multiple implementations of Notification Service
public class EmailNotification implements NotificationService {
    @Override
    public void sendNotification(String msg) {
        System.out.println("Sending Email notification: "+ msg);
    }
}
