package com.asm5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.asm5", "com.otherpackage"})
public class Asmj5Application {

    public static void main(String[] args) {
        SpringApplication.run(Asmj5Application.class, args);
    }

}
