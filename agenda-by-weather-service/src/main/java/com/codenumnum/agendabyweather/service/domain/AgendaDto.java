package com.codenumnum.agendabyweather.service.domain;

import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDay;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@Slf4j
public record AgendaDto(@Delegate @JsonIgnore Agenda agenda, Map<String, AgendaDayDto> agendaDaysByDateString) {

    @JsonIgnore
    public Set<AgendaDay> getAgendaDays() {
        return agenda.getAgendaDays();
    }
}
