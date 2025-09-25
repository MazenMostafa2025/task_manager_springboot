package com.mazen.wfm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Manager API")
                        .version("1.0")
                        .description("API Documentation for Task Manager built with Spring Boot"))
                .components(new Components()
                            .addSchemas("ErrorResponse", new Schema<>().type("object")
                                .addProperties("success", new Schema<>().type("boolean").example(false))
                                .addProperties("message", new Schema<>().type("string").example("Bad Request"))
                                .addProperties("data", new Schema<>().type("object"))
                                .addProperties("timestamp", new Schema<>().type("string").format("date-time")
                                        .example("2025-09-22T13:47:15.123")))
                                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                                .name("bearerAuth")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT"))
                            .addResponses("BadRequestResponse",
                                new io.swagger.v3.oas.models.responses.ApiResponse()
                                        .description("Bad Request")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse").example(Map.of( "success", false,
                                                        "message", "Bad Request",
                                                        "data", new Object(),
                                                        "timestamp", "2025-09-22T13:47:15.123"))))))
                            .addResponses("NotFoundResponse",
                                new io.swagger.v3.oas.models.responses.ApiResponse()
                                        .description("Not Found")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse").example(Map.of( "success", false,
                                                        "message", "Some Resource was not found",
                                                        "data", new Object(),
                                                        "timestamp", "2025-09-22T13:47:15.123"))))))
                            .addResponses("InternalErrorResponse",
                                new io.swagger.v3.oas.models.responses.ApiResponse()
                                        .description("Internal Server Error")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse").example(Map.of( "success", false,
                                                        "message", "error occurred on the server",
                                                        "data", new Object(),
                                                        "timestamp", "2025-09-22T13:47:15.123")))))))
                        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}