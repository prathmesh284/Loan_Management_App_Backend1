package com.example.LoanManagementApp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;

/**
 * Jackson Configuration
 * Registers custom deserializers and serializers for date handling
 */
@Configuration
public class JacksonConfig {

    /**
     * Configure Jackson ObjectMapper with custom LocalDate deserializer
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();

        // Create a module for custom deserializations
        SimpleModule module = new SimpleModule();
        
        // Register the custom LocalDate deserializer
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());

        // Register the module with the ObjectMapper
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
