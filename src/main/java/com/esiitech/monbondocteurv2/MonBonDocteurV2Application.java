package com.esiitech.monbondocteurv2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MonBonDocteurV2Application {

	public static void main(String[] args) {
		SpringApplication.run(MonBonDocteurV2Application.class, args);
	}

}
