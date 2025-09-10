package com.oranba.springboot.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCaching // Enable Spring Cache abstraction
@ComponentScan(basePackages = { "com.oranba.springboot.catalog" })
public class ProductCatalogApplication {

	public static void main(String[] args) {
        SpringApplication.run(ProductCatalogApplication.class, args);
	}

}
