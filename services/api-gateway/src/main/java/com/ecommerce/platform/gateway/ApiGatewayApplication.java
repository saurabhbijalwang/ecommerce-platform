package com.ecommerce.platform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Client-facing HTTP requests ko Eureka par register services tak route karta hai.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /** Spring Boot application context aur reactive gateway start karta hai. */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
