package com.codenumnum.agendabyweather.service.domain;

import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
public record AgendaDto(String latLon,
                        boolean defaultAgenda,
                        ZoneOffset offset,
                        String hourlyWeatherForecastUrl,
                        OffsetDateTime hourlyWeatherGeneratedAt,
                        OffsetDateTime hourlyWeatherUpdateTime,
                        String generalWeatherForecastUrl,
                        OffsetDateTime generalWeatherGeneratedAt,
                        OffsetDateTime generalWeatherUpdateTime,
                        List<AgendaItem> agendaItems,
                        Map<LocalDate, AgendaDayDto> agendaDaysByDay) {

    public void runArchivalProcess(LocalDate startDate, ObjectMapper objectMapper) {
        if(CollectionUtils.isEmpty(agendaDaysByDay)) {
            return;
        }

        List<LocalDate> currentAndPastAgendaDateDays =
                agendaDaysByDay.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .filter(entry -> entry.getKey().isBefore(startDate) || entry.getKey().isEqual(startDate))
                        .map(Map.Entry::getKey)
                        .toList();

        for(int i = currentAndPastAgendaDateDays.size() - 1; i >= 0; i--) {
            AgendaDayDto currentDayDto = agendaDaysByDay.get(currentAndPastAgendaDateDays.get(i));
            if(!currentDayDto.archiveGeneralWeatherPeriods(objectMapper)) {
                break;
            }
        }
    }
}
