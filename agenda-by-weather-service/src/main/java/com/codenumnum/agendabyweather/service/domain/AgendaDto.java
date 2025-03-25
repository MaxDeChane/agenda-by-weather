package com.codenumnum.agendabyweather.service.domain;

import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
@Builder
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

        LocalDate currentDate = LocalDate.now(offset);

        for(int i = currentAndPastAgendaDateDays.size() - 1; i >= 0; i--) {
            AgendaDayDto currentDayDto = agendaDaysByDay.get(currentAndPastAgendaDateDays.get(i));

            // Break if there were no updates and the date is not today. This short-circuits the process, preventing unnecessary checks
            // on entries that have already been updated. However, if the date is today and no updates have been made yet, continue checking,
            // as updates may still occur if there are earlier days.
            if(!currentDayDto.archiveGeneralWeatherPeriods(objectMapper) && currentDate.isBefore(currentAndPastAgendaDateDays.get(i))) {
                break;
            }
        }
    }
}
