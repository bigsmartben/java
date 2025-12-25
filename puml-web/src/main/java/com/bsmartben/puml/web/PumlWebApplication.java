package com.bsmartben.puml.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Main Application Entry Point
 * Excludes default DataSource auto-configuration to prevent conflicts with dual datasource setup
 */
@SpringBootApplication(
    scanBasePackages = "com.bsmartben.puml",
    exclude = {DataSourceAutoConfiguration.class}
)
public class PumlWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(PumlWebApplication.class, args);
    }
}
