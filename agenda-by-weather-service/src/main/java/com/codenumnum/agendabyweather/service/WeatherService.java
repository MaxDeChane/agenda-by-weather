package com.codenumnum.agendabyweather.service;

import com.codenumnum.agendabyweather.dao.WeatherApiDao;
import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import com.codenumnum.agendabyweather.dao.domain.WeatherForecastPeriod;
import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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

    public Map<LocalDate, Map<OffsetDateTime, WeatherForecastPeriod>> mapWeatherPeriodsByDay(List<WeatherForecastPeriod> weatherForecastPeriods) {
        LocalDate currentDayDate = null;
        Map<OffsetDateTime, WeatherForecastPeriod> currentDayPeriodsByDateTime = null;
        Map<LocalDate, Map<OffsetDateTime, WeatherForecastPeriod>> periodsByDay = new HashMap<>();
        for(WeatherForecastPeriod weatherForecastPeriod : weatherForecastPeriods) {
            LocalDate nextDayDate = weatherForecastPeriod.startTime().toLocalDate();

            if(!nextDayDate.equals(currentDayDate)) {
                currentDayDate = nextDayDate;
                currentDayPeriodsByDateTime = new HashMap<>();
                periodsByDay.put(currentDayDate, currentDayPeriodsByDateTime);
            }

            currentDayPeriodsByDateTime.put(weatherForecastPeriod.startTime(), weatherForecastPeriod);
        }

        return periodsByDay;
    }
}
