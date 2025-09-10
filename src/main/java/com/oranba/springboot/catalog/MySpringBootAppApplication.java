package com.oranba.springboot.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.oranba.springboot.mySpringBootApp", "health"})
public class MySpringBootAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MySpringBootAppApplication.class, args);
	}

}
