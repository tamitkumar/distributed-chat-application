package com.techbrain.chat.cofig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class AppConfig {

    /**
     * Primary ObjectMapper for general use (WebSocket, REST, etc.)
     * Does NOT include polymorphic type handling
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register Java 8 date/time module for LocalDateTime support
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Special ObjectMapper for Redis serialization only
     * Includes polymorphic type handling for storing complex objects
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());

        // Enable polymorphic type handling for Redis storage
        // This adds type information to JSON for deserialization
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        return mapper;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Distributed Chat Application API")
                        .version("1.0.0")
                        .description("Real-time multi-client distributed chat application")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Server"),
                        new Server().url("http://uat.example.com").description("UAT Server"),
                        new Server().url("http://prod.example.com").description("Production Server")
                ));
    }
}
