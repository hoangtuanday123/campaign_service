package com.example.campaignservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campaignOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Campaign Service API")
                        .description("Campaign lifecycle management for the promotion platform")
                        .version("v1")
                        .contact(new Contact().name("Platform Backend Team")));
    }
}