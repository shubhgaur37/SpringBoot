package com.module1.shubh.module1Intro;

//@Component doesn't make sense here as we cannot have an object of an interface
public interface NotificationService {
    public void sendNotification(String msg);
}


//SMSNotification:
//Did not match:
//        - @ConditionalOnProperty (notification.type=sms) found different value in property 'notification.type' (OnPropertyCondition)

// SMS bean was not created because it did not satisfy the conditional