package com.app.domoticaweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DomoticaWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomoticaWebApplication.class, args);
    }

}
