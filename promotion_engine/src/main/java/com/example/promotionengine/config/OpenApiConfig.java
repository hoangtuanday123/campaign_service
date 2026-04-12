package com.example.promotionengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI promotionEngineOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Promotion Engine API")
                        .description("Real-time promotion eligibility evaluation for the campaign platform")
                        .version("v1")
                        .contact(new Contact().name("Platform Backend Team")));
    }
}