package com.module1.shubh.module1Intro.concrete_notification_services;

import com.module1.shubh.module1Intro.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;


// @Qualifier: helps you distinguish between multiple beans of the same type. Notification Service in this case
@Component
@Qualifier("emailNotif")
@ConditionalOnProperty(name = "notification.type",havingValue = "email")
public class EmailNotification implements NotificationService {
    @Override
    public void sendNotification(String msg) {
        System.out.println("Sending Email notification: "+ msg);
    }
}

//@ConditionalOnProperty annotation specifies when should a specific bean be created based on
//configurations defined in application.properties file