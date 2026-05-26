package com.example.polarionprocessor;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ModuleProcessorProperties.class)
public class PolarionModuleProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolarionModuleProcessorApplication.class, args);
    }
}
