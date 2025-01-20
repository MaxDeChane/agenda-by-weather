package com.codenumnum.agendabyweather.dao;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class WeatherApiDao {

    WebClient webClient;
    String weatherPointsUrl;

    public WeatherApiDao(WebClient.Builder webClientBuilder,
                         @Value("${weather-api.points.url}") String weatherPointsUrl) {
        this.webClient = webClientBuilder.build();
        this.weatherPointsUrl = weatherPointsUrl;
    }

    public WeatherForecast retrieveHourlyForecast(String latLon) {
        // TODO: move this into user info setup so it will only need to be done one time when that stuff gets setup
         WeatherUrls weatherUrls = webClient.get()
                .uri(weatherPointsUrl, uriBuilder -> uriBuilder.build(latLon))
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
