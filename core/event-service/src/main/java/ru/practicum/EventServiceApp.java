package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.feign.client.RequestClient;
import ru.practicum.feign.client.UserClient;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients(clients = {StatsClient.class, UserClient.class, RequestClient.class})
public class EventServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApp.class, args);
    }
}