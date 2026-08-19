package com.company.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

import static org.springframework.boot.WebApplicationType.SERVLET;

@EnableIntegration
@IntegrationComponentScan
@SpringBootApplication
public class ApiAppApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplicationBuilder(ApiAppApplication.class)
            .web(SERVLET)
            .build();
        app.run( args);
    }

}
