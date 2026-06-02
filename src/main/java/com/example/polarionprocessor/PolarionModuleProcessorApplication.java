package com.example.polarionprocessor;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.config.PolarionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({ModuleProcessorProperties.class, PolarionProperties.class})
public class PolarionModuleProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PolarionModuleProcessorApplication.class, args);
    }
}
