package com.module1.shubh.module1Intro.concrete_notification_services;

import com.module1.shubh.module1Intro.NotificationService;
import org.springframework.stereotype.Component;


@Component
public class EmailNotification implements NotificationService {
    @Override
    public void sendNotification(String msg) {
        System.out.println("Sending Email notification: "+ msg);
    }
}
