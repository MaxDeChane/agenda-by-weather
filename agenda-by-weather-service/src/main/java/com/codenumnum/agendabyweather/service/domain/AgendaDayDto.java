package com.codenumnum.agendabyweather.service.domain;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecastPeriod;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDay;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDayKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import org.springframework.util.CollectionUtils;

import java.util.*;

public record AgendaDayDto(
        @Delegate @JsonIgnore AgendaDay agendaDay,
        Map<String, WeatherForecastPeriod> generalWeatherPeriods,
        Map<String, WeatherForecastPeriod> hourlyWeatherPeriods) {

    @SneakyThrows
    public AgendaDayDto updateForecast(Map<String, WeatherForecastPeriod> forecastPeriodsByStartTime, boolean isGeneralForecast,
                                       ObjectMapper objectMapper) {
        Map<String, WeatherForecastPeriod> currentForecastPeriods = (isGeneralForecast) ? this.generalWeatherPeriods : this.hourlyWeatherPeriods;
        if(CollectionUtils.isEmpty(currentForecastPeriods)) {
            String forecastPeriodsByStartTimeJson = objectMapper.writeValueAsString(forecastPeriodsByStartTime);
            if(isGeneralForecast) {
                agendaDay.setGeneralWeatherForecastJson(forecastPeriodsByStartTimeJson);
                return new AgendaDayDto(agendaDay, forecastPeriodsByStartTime, this.hourlyWeatherPeriods);
            }
            agendaDay.setHourlyWeatherForecastJson(forecastPeriodsByStartTimeJson);
            return new AgendaDayDto(agendaDay, this.generalWeatherPeriods, forecastPeriodsByStartTime);
        }

        forecastPeriodsByStartTime.keySet().stream().sorted().forEach(key -> {
            if(currentForecastPeriods.containsKey(key)) {
                currentForecastPeriods.put(key, forecastPeriodsByStartTime.get(key));
            } else {
                Optional<String> keyOfPeriodToUpdate =
                        currentForecastPeriods.entrySet().stream()
                                .filter(forecastPeriodEntry -> {
                                    WeatherForecastPeriod currentPeriod = forecastPeriodEntry.getValue();

                                    return currentPeriod.startTime().compareTo(forecastPeriodEntry.getKey()) <= 0 &&
                                            currentPeriod.endTime().compareTo(forecastPeriodEntry.getKey()) >= 0;
                                })
                                .map(Map.Entry::getKey)
                                .findFirst();

                if(keyOfPeriodToUpdate.isPresent()) {
                    WeatherForecastPeriod periodWithUpdatedEndTime = currentForecastPeriods.get(keyOfPeriodToUpdate.get());
                    forecastPeriodsByStartTime.put(keyOfPeriodToUpdate.get(), periodWithUpdatedEndTime.updateEndDateTime(key));
                }
                currentForecastPeriods.put(key, forecastPeriodsByStartTime.get(key));
            }
        });

        String forecastPeriodsByStartTimeJson = objectMapper.writeValueAsString(currentForecastPeriods);
        if(isGeneralForecast) {
            agendaDay.setGeneralWeatherForecastJson(forecastPeriodsByStartTimeJson);
        } else {
            agendaDay.setHourlyWeatherForecastJson(forecastPeriodsByStartTimeJson);
        }

        return this;
    }

    @JsonIgnore
    public AgendaDayKey getId() {
        return agendaDay.getId();
    }

    @JsonIgnore
    public String getGeneralWeatherForecastJson() {
        return agendaDay.getGeneralWeatherForecastJson();
    }

    @JsonIgnore
    public String getHourlyWeatherForecastJson() {
        return agendaDay.getHourlyWeatherForecastJson();
    }
}
