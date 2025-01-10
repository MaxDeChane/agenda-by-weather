package com.codenumnum.agendabyweather.service;

import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.repository.AgendaRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class AgendaService {

    AgendaRepository agendaRepository;

    public Agenda retrieveAgendaCreatingIfNotExists(String latLon) {
        var agendaOptional = agendaRepository.findById(latLon);

        if (agendaOptional.isPresent()) {
            return agendaOptional.get();
        }

        Agenda agenda = new Agenda(latLon);
        return agendaRepository.save(agenda);
    }
}
