package com.example.campaignservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campaignOpenApi() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
                .addSecuritySchemes(
                    "apiKeyAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .addSecurityItem(new SecurityRequirement().addList("apiKeyAuth"))
                .info(new Info()
                        .title("Campaign Service API")
                        .description("Campaign lifecycle management for the promotion platform")
                        .version("v1")
                        .contact(new Contact().name("Platform Backend Team")));
    }
}