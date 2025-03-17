package com.codenumnum.agendabyweather.service.domain.factory;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecastPeriod;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDay;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDayKey;
import com.codenumnum.agendabyweather.service.domain.AgendaDayDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.*;
import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class AgendaDayFactory {

    ObjectMapper objectMapper;

    @SneakyThrows
    public AgendaDayDto createNewDtoWithWeatherForecast(LocalDate dayDate, Map<OffsetDateTime, WeatherForecastPeriod> forecastPeriods,
                                                        boolean isGeneralForecast) {
        if(isGeneralForecast) {
            return new AgendaDayDto(dayDate, forecastPeriods, Collections.emptyMap());
        } else {
            return new AgendaDayDto(dayDate, Collections.emptyMap(), forecastPeriods);
        }
    }

    @SneakyThrows
    public AgendaDayDto createDtoFromEntity(AgendaDay agendaDay) {
        Map<OffsetDateTime, WeatherForecastPeriod> generalPeriodsByStartTime;
        Map<OffsetDateTime, WeatherForecastPeriod> hourlyPeriodsByStartTime;

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

        return new AgendaDayDto(agendaDay.getAgendaDayKey().getDayDate().toLocalDate(), generalPeriodsByStartTime, hourlyPeriodsByStartTime);
    }

    @SneakyThrows
    public AgendaDay createEntityFromDto(AgendaDayDto agendaDayDto, ZoneOffset offset, UUID agendaId) {

        AgendaDayKey agendaDayKey = new AgendaDayKey(agendaId, agendaDayDto.dayDate().atTime(LocalTime.MIDNIGHT)
                .atOffset(offset));

        String generalWeatherPeriodsJson = objectMapper.writeValueAsString(agendaDayDto.generalWeatherPeriods());
        String hourlyWeatherPeriodsJson = objectMapper.writeValueAsString(agendaDayDto.hourlyWeatherPeriods());

        return  AgendaDay.builder()
                .agendaDayKey(agendaDayKey)
                .generalWeatherForecastJson(generalWeatherPeriodsJson)
                .hourlyWeatherForecastJson(hourlyWeatherPeriodsJson)
                .build();
    }
}
