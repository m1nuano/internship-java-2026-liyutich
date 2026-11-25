package com.demo.senla;

import com.demo.senla.notification.NotificationClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = NotificationClient.class)
public class SenlaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SenlaApplication.class, args);
    }

}
