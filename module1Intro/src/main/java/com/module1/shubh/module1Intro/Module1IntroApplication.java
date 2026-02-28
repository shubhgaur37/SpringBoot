package com.module1.shubh.module1Intro;

import com.module1.shubh.module1Intro.concrete_notification_services.SMSNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Module1IntroApplication implements CommandLineRunner {
//	Injecting notification service avoiding tight-coupling
	@Autowired
	NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(Module1IntroApplication.class, args);

	}

//	Executed by spring framework when everything is initialised, all beans are present,
//	All command line runners will start running
//	Also this method is not static which allows us to call methods on payment service without
//	making it static which was required when we wanted to run it in main[because of it being static]
	@Override
	public void run(String... args) throws Exception {
		// tight coupling
//		NotificationService notificationService = new SMSNotification();
		notificationService.sendNotification("Hello World!");
	}
}
