package com.module1.shubh.module1Intro;

//@Component doesn't make sense here as we cannot have an object of an interface
public interface NotificationService {
    public void sendNotification(String msg);
}
