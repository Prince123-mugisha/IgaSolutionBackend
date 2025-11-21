package com.work.IGA.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register JSR310 module for Java 8 time support
        mapper.registerModule(new JavaTimeModule());
        
        // Configure Jackson to handle Hibernate lazy loading issues
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // Disable writing dates as timestamps (write as strings instead)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
}