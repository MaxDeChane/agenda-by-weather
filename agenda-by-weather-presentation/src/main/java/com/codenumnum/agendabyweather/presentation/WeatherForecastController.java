package com.codenumnum.agendabyweather.presentation;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import com.codenumnum.agendabyweather.service.WeatherService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/weather")
public class WeatherForecastController {

    WeatherService weatherService;

    @GetMapping
    public WeatherForecast getUserWeatherForecast() {
        return weatherService.retrieveUserMainAreaWeather();
    }
}
