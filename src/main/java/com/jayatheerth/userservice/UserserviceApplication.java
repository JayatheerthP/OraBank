package com.jayatheerth.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the User Service microservice.
 * This class serves as the entry point for the Spring Boot application,
 * initializing the application context and starting the service.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserserviceApplication {
	/**
	 * Main method to start the User Service application.
	 * This method initializes and runs the Spring Boot application context.
	 *
	 * @param args Command-line arguments passed to the application.
	 */
	public static void main(String[] args) {
		SpringApplication.run(UserserviceApplication.class, args);
	}

}
