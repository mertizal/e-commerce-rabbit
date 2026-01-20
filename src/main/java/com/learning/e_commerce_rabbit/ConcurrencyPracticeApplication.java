package com.learning.e_commerce_rabbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class ConcurrencyPracticeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcurrencyPracticeApplication.class, args);
	}

}
