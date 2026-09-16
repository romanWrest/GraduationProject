package ru.dstu.dormitory.requests_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.dstu.dormitory.requests_service.client")
@EnableScheduling
public class RequestsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequestsServiceApplication.class, args);
    }
}
