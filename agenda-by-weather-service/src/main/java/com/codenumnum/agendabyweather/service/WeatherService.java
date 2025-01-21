package com.codenumnum.agendabyweather.service;

import com.codenumnum.agendabyweather.dao.WeatherApiDao;
import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class WeatherService {

    WeatherApiDao weatherApiDao;

    public WeatherUrls retrieveWeatherUrls(String latLon) {
        return weatherApiDao.retrieveWeatherUrls(latLon);
    }

    public WeatherForecast retrieveWeatherForecast(String weatherUrl) {
        return weatherApiDao.retrieveWeatherForecast(weatherUrl);
    }
}
