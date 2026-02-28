package com.module1.shubh.module1Intro.concrete_notification_services;

import com.module1.shubh.module1Intro.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("smsNotif")
public class SMSNotification implements NotificationService {
    @Override
    public void sendNotification(String msg) {
        System.out.println("Sending SMS notification: "+ msg);
    }
}
