package com.module2.shubh.SpringBootWebTutorial.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper getModelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ObjectMapper getObjectMapper() {
//        this overrides spring boot's autoconfiguration for object mapper
        ObjectMapper objectMapper = new ObjectMapper();
//        register java time module with object mapper
        objectMapper.registerModule(new JavaTimeModule()); // returning dates in timestamp array format
// changing format of timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
