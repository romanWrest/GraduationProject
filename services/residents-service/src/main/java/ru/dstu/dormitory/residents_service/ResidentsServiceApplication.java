package ru.dstu.dormitory.residents_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.dstu.dormitory.residents_service.client")
@EnableScheduling
public class ResidentsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResidentsServiceApplication.class, args);
    }
}