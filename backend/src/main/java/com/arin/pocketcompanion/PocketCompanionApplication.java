package com.arin.pocketcompanion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PocketCompanionApplication {
    public static void main(String[] args) {
        SpringApplication.run(PocketCompanionApplication.class, args);
    }
}
