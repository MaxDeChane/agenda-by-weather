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

    public WeatherUrls retrieveWeatherUrls(String latLon) {
        return webClient.get()
                .uri(weatherPointsUrl, uriBuilder -> uriBuilder.build(latLon))
                .retrieve()
                .bodyToMono(WeatherUrls.class)
                .block();
    }

    public WeatherForecast retrieveWeatherForecast(String weatherForecastUrl) {
         return webClient.get()
                 .uri(weatherForecastUrl)
                 .retrieve()
                 .bodyToMono(WeatherForecast.class)
                 .block();
    }
}
