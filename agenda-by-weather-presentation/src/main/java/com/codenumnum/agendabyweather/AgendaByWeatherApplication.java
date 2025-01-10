package com.codenumnum.agendabyweather;

import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.repository.AgendaRepository;
import com.codenumnum.agendabyweather.service.AgendaService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

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

    @Bean
    public Agenda agenda(AgendaService agendaService) {
        return agendaService.retrieveAgendaCreatingIfNotExists("39.6677,-103.5934");
    }

    public static void main(String[] args) {
        SpringApplication.run(AgendaByWeatherApplication.class, args);
    }
}
