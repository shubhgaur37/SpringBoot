package com.module1.shubh.module1Intro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Module1IntroApplication implements CommandLineRunner {
//	Now for some reason I want to send notification on all channels, then that would require me to get a hold
//	of all implementations of Notification service, which can be done using a map
//	we cannot directly use Autowired here, because we have not defined a bean for Map
//	so we need to initialise the map first and then it will contain all objects of notification service
	@Autowired
	Map<String,NotificationService> notificationServiceMap = new HashMap<>();

	public static void main(String[] args) {
		SpringApplication.run(Module1IntroApplication.class, args);

	}

//	Executed by spring framework when everything is initialised, all beans are present,
//	All command line runners will start running
//	Also this method is not static which allows us to call methods on payment service without
//	making it static which was required when we wanted to run it in main[because of it being static]
	@Override
	public void run(String... args) throws Exception {
		for(Map.Entry<String,NotificationService> entry : notificationServiceMap.entrySet()){
//			type of notification service
			System.out.println(entry.getKey());
//			send notification on the channel
			entry.getValue().sendNotification("Hello");
		}
	}

//	Output
//	emailNotification
//	Sending Email notification: Hello
//
//	SMSNotification
//	Sending SMS notification: Hello
}
