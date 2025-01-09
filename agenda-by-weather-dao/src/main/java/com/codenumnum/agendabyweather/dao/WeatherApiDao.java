package com.codenumnum.agendabyweather.dao;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class WeatherApiDao {

    public static final String WEATHER_POINTS_URL = "https://api.weather.gov/points/{latitude},{longitude}";
    WebClient webClient;

    public WeatherApiDao(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public WeatherForecast retrieveHourlyForecast(double latitude, double longitude) {
        // TODO: move this into user info setup so it will only need to be done one time when that stuff gets setup
         WeatherUrls weatherUrls = webClient.get()
                .uri(WEATHER_POINTS_URL, uriBuilder -> uriBuilder.build(latitude, longitude))
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
