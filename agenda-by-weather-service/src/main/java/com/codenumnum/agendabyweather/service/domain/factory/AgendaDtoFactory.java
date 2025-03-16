package com.codenumnum.agendabyweather.service.domain.factory;

import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDay;
import com.codenumnum.agendabyweather.service.domain.AgendaDayDto;
import com.codenumnum.agendabyweather.service.domain.AgendaDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class AgendaDtoFactory {

    AgendaDayDtoFactory agendaDayDtoFactory;

    public AgendaDto createFromExistingAgenda(Agenda agenda, ObjectMapper objectMapper) {
        if(CollectionUtils.isEmpty(agenda.getAgendaDays())) {
            return new AgendaDto(agenda, new HashMap<>());
        }

        Map<LocalDate, AgendaDayDto> agendaDaysByDateString = new HashMap<>();
        for(AgendaDay agendaDay : agenda.getAgendaDays()) {
            AgendaDayDto agendaDayDto = agendaDayDtoFactory.createFromAgendaDay(agendaDay, objectMapper);
            agendaDaysByDateString.put(agendaDayDto.getAgendaDayKey().getDayDate().toLocalDate(), agendaDayDto);
        }

        return new AgendaDto(agenda, agendaDaysByDateString);
    }
}
