package com.example.Module1.Practice.Bakery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Bakery implements CommandLineRunner {

    Syrup syrup;
    Frosting frosting;

    public Bakery(Syrup syrup, Frosting frosting) {
        this.syrup = syrup;
        this.frosting = frosting;
    }

    public static void main(String[] args) {
        SpringApplication.run(Bakery.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("BakeryApplication started");
        System.out.println("SYRUP TYPE: "+syrup.getSyrupType());
        System.out.println("FROSTING TYPE: "+frosting.getFrostingType());
    }
}
