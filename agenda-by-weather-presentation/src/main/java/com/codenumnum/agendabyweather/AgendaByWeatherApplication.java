package com.codenumnum.agendabyweather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * This is put in the presentation layer since everything will be built upwards
 * with the dependencies coming downwards.
 */
@SpringBootApplication
public class AgendaByWeatherApplication {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(AgendaByWeatherApplication.class, args);
    }
}
