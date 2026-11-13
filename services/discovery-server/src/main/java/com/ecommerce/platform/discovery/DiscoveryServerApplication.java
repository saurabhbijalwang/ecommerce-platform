package com.ecommerce.platform.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Platform services ke registration aur discovery ke liye Eureka registry chalata hai.
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    /** Spring Boot application context aur Eureka server start karta hai. */
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
