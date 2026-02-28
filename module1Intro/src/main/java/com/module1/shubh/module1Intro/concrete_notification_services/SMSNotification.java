package com.module1.shubh.module1Intro.concrete_notification_services;

import com.module1.shubh.module1Intro.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Qualifier("smsNotif")
//@ConditionalOnProperty(name = "notification.type",havingValue = "sms")
// If the property is not defined in application.properties, then also the bean won't be created
public class SMSNotification implements NotificationService {
    @Override
    public void sendNotification(String msg) {
        System.out.println("Sending SMS notification: "+ msg);
    }
}



