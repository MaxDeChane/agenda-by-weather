package com.codenumnum.agendabyweather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is put in the presentation layer since everything will be built upwards
 * with the dependencies coming downwards.
 */
@SpringBootApplication
public class AgendaByWeatherPresentationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgendaByWeatherPresentationApplication.class, args);
    }
}
