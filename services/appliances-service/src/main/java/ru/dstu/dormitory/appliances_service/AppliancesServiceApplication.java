package ru.dstu.dormitory.appliances_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.dstu.dormitory.appliances_service.client")
@EnableScheduling
public class AppliancesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppliancesServiceApplication.class, args);
    }
}
