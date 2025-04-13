package com.example.ourLog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ourLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(ourLogApplication.class, args);
		System.out.println("http://localhost:8080/ourLog");
	}

}
