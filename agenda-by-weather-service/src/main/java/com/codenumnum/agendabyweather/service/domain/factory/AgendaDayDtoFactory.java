package com.codenumnum.agendabyweather.service.domain.factory;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecastPeriod;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDay;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDayKey;
import com.codenumnum.agendabyweather.service.domain.AgendaDayDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

@Component
public class AgendaDayDtoFactory {

    @SneakyThrows
    public AgendaDayDto createNewWithWeatherForecast(UUID agendaId, String day, Map<String, WeatherForecastPeriod> forecastPeriods,
                                                            boolean isGeneralForecast, ObjectMapper objectMapper) {
        AgendaDayKey agendaDayKey = new AgendaDayKey(agendaId, day);
        AgendaDay.AgendaDayBuilder agendaDayBuilder = AgendaDay.builder()
                .id(agendaDayKey);

        String forecastPeriodsString = objectMapper.writeValueAsString(forecastPeriods);
        if(isGeneralForecast) {
            agendaDayBuilder.generalWeatherForecastJson(forecastPeriodsString);
            return new AgendaDayDto(agendaDayBuilder.build(), forecastPeriods, Collections.emptyMap());
        } else {
            agendaDayBuilder.hourlyWeatherForecastJson(forecastPeriodsString);
            return new AgendaDayDto(agendaDayBuilder.build(), Collections.emptyMap(), forecastPeriods);
        }
    }

    @SneakyThrows
    public AgendaDayDto createFromAgendaDay(AgendaDay agendaDay, ObjectMapper objectMapper) {
        Map<String, WeatherForecastPeriod> generalPeriodsByStartTime;
        Map<String, WeatherForecastPeriod> hourlyPeriodsByStartTime;

        if(StringUtils.hasText(agendaDay.getGeneralWeatherForecastJson())) {
            generalPeriodsByStartTime = objectMapper.readValue(agendaDay.getGeneralWeatherForecastJson(), new TypeReference<>() {});
        } else {
            generalPeriodsByStartTime = new HashMap<>();
        }

        if(StringUtils.hasText(agendaDay.getHourlyWeatherForecastJson())) {
            hourlyPeriodsByStartTime = objectMapper.readValue(agendaDay.getHourlyWeatherForecastJson(), new TypeReference<>() {});
        } else {
            hourlyPeriodsByStartTime = new HashMap<>();
        }

        return new AgendaDayDto(agendaDay, generalPeriodsByStartTime, hourlyPeriodsByStartTime);
    }
}
