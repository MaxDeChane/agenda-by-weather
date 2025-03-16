package com.codenumnum.agendabyweather.service.domain;

import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDay;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public record AgendaDto(@Delegate @JsonIgnore Agenda agenda, Map<LocalDate, AgendaDayDto> agendaDaysByDay) {

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

    @JsonIgnore
    public Set<AgendaDay> getAgendaDays() {
        return agenda.getAgendaDays();
    }
}
