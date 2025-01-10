package com.codenumnum.agendabyweather.dao;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class WeatherApiDao {

    WebClient webClient;
    Agenda agenda;
    String WEATHER_POINTS_URL;

    public WeatherApiDao(WebClient.Builder webClientBuilder, Agenda agenda,
                         @Value("${weather-api.points.url}") String weatherPointsUrl) {
        this.webClient = webClientBuilder.build();
        this.agenda = agenda;
        WEATHER_POINTS_URL = weatherPointsUrl;
    }

    public WeatherForecast retrieveHourlyForecast() {
        // TODO: move this into user info setup so it will only need to be done one time when that stuff gets setup
         WeatherUrls weatherUrls = webClient.get()
                .uri(WEATHER_POINTS_URL, uriBuilder -> uriBuilder.build(agenda.getLatLon()))
                .retrieve()
                .bodyToMono(WeatherUrls.class)
                .block();

         return webClient.get()
                 .uri(weatherUrls.getForecastHourlyUrl())
                 .retrieve()
                 .bodyToMono(WeatherForecast.class)
                 .block();
    }
}
