package com.EduPulse.ClassService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.EduPulse.ClassService.service" )

public class ClassServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClassServiceApplication.class, args);
	}

}
