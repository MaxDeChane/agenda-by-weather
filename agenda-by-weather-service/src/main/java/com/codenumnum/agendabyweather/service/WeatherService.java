package com.codenumnum.agendabyweather.service;

import com.codenumnum.agendabyweather.dao.WeatherApiDao;
import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import com.codenumnum.agendabyweather.dao.domain.WeatherForecastPeriod;
import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class WeatherService {

    DateTimeService dateTimeService;
    WeatherApiDao weatherApiDao;

    public WeatherUrls retrieveWeatherUrls(String latLon) {
        return weatherApiDao.retrieveWeatherUrls(latLon);
    }

    public WeatherForecast retrieveWeatherForecast(String weatherUrl) {
        return weatherApiDao.retrieveWeatherForecast(weatherUrl);
    }

    public Map<String, Map<String, WeatherForecastPeriod>> mapWeatherPeriodsByDay(List<WeatherForecastPeriod> weatherForecastPeriods) {
        String currentDayDate = null;
        Map<String, WeatherForecastPeriod> currentDayPeriodsByDateTimeString = null;
        Map<String, Map<String, WeatherForecastPeriod>> periodsByDay = new HashMap<>();
        for(WeatherForecastPeriod weatherForecastPeriod : weatherForecastPeriods) {
            String nextDayDate = dateTimeService.retrieveDayPartFromUtcDateTime(weatherForecastPeriod.startTime());

            if(!nextDayDate.equals(currentDayDate)) {
                currentDayDate = nextDayDate;
                currentDayPeriodsByDateTimeString = new HashMap<>();
                periodsByDay.put(currentDayDate, currentDayPeriodsByDateTimeString);
            }

            currentDayPeriodsByDateTimeString.put(weatherForecastPeriod.startTime(), weatherForecastPeriod);
        }

        return periodsByDay;
    }
}
