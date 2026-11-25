package com.demo.senla.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI travelServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Travel Service API")
                .version("1.0")
                .description("Employees, departments, travel requests and transport"));
    }
}
