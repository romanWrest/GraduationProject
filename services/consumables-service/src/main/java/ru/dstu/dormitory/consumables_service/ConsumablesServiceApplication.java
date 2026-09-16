package ru.dstu.dormitory.consumables_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.dstu.dormitory.consumables_service.client")
@EnableScheduling
public class ConsumablesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumablesServiceApplication.class, args);
    }
}
