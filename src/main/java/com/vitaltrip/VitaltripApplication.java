package com.vitaltrip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VitaltripApplication {
    public static void main(String[] args) {
        SpringApplication.run(VitaltripApplication.class, args);
    }
}
