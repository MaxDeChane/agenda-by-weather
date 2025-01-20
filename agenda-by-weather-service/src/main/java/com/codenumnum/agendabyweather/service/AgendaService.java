package com.codenumnum.agendabyweather.service;

import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.repository.AgendaRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class AgendaService {

    AgendaRepository agendaRepository;

    public Agenda retrieveDefaultAgendaCreatingIfNotPresent() {
        var agendaOptional = agendaRepository.findByDefaultAgenda(true);

        if (agendaOptional.isPresent()) {
            log.info("Found default agenda");
            return agendaOptional.get();
        }

        log.info("Default agenda not found so creating new one");
        // Initial will have a value of empty so the front end knows to get
        // the info from the user.
        Agenda agenda = new Agenda("", true, null);
        return agendaRepository.save(agenda);
    }

    public Agenda updateDefaultAgendaLatLon(String latLon) {
        Agenda defaultAgenda = retrieveDefaultAgendaCreatingIfNotPresent();
        defaultAgenda.setLatLon(latLon);

        return agendaRepository.save(defaultAgenda);
    }
}
