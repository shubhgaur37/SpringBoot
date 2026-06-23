package com.shubh.module4.Prod_Ready_Features.config;

import com.shubh.module4.Prod_Ready_Features.auth.AuditorAwareImpl;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
// pass bean name[same as config class getter method]
// for auditor aware implementation
@EnableJpaAuditing(auditorAwareRef = "getAuditorAwareImplementation")
public class AppConfig {

    @Bean
    ModelMapper getModelMapper() {
        return new ModelMapper();
    }

    @Bean
    AuditorAware<String> getAuditorAwareImplementation() {
        return new AuditorAwareImpl();
    }
}
