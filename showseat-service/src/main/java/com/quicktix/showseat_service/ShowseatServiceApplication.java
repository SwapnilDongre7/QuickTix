package com.quicktix.showseat_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.quicktix.showseat_service.repository")
@EnableMongoAuditing
@EnableScheduling
public class ShowseatServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShowseatServiceApplication.class, args);
	}

}
