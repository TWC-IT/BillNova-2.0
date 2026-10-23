package com.thewizecompany.billnova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
//@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
@ConfigurationPropertiesScan
public class BillnovaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillnovaApplication.class, args);
    }

}
