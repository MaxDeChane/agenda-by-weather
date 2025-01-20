package com.codenumnum.agendabyweather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This is put in the presentation layer since everything will be built upwards
 * with the dependencies coming downwards.
 */
@SpringBootApplication
public class AgendaByWeatherApplication {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedMethods("*").allowedOrigins("http://localhost:3001");
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(AgendaByWeatherApplication.class, args);
    }
}
