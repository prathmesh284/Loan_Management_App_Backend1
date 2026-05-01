package com.example.LoanManagementApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoanManagementAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanManagementAppApplication.class, args);
	}
}
