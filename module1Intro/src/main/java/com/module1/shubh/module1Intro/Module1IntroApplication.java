package com.module1.shubh.module1Intro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Module1IntroApplication implements CommandLineRunner {
	// unless I use @autowired annotation, I will get a null pointer exception
	@Autowired
	PaymentService paymentService1;

	@Autowired
	PaymentService paymentService2;


	public static void main(String[] args) {
		SpringApplication.run(Module1IntroApplication.class, args);
//		Not Initialised Error, we have to explicitly tell the framework to inject the bean
//		PaymentService paymentService;
//		paymentService.pay();

	}

//	Executed by spring framework when everything is initialised, all beans are present,
//	All command line runners will start running
//	Also this method is not static which allows us to call methods on payment service without
//	making it static which was required when we wanted to run it in main[because of it being static]
	@Override
	public void run(String... args) throws Exception {
		paymentService1.pay();
		paymentService2.pay();
//		checking object identifiers for payment service instances
		System.out.println(paymentService1.hashCode());
		System.out.println(paymentService2.hashCode());
	}
}
