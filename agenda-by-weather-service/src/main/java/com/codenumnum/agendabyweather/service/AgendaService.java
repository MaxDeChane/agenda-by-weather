package com.codenumnum.agendabyweather.service;

import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.codenumnum.agendabyweather.dao.repository.AgendaItemRepository;
import com.codenumnum.agendabyweather.dao.repository.AgendaRepository;
import com.codenumnum.agendabyweather.service.domain.AddAgendaItemStatusEnum;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class AgendaService {

    AgendaRepository agendaRepository;
    AgendaItemRepository agendaItemRepository;

    public Agenda retrieveDefaultAgendaCreatingIfNotPresent() {
        var agendaOptional = agendaRepository.findByDefaultAgenda(true);

        if (agendaOptional.isPresent()) {
            log.info("Found default agenda");
            return agendaOptional.get();
        }

        log.info("Default agenda not found so creating new one");
        // Initial will have a value of empty so the front end knows to get
        // the info from the user.
        Agenda.AgendaBuilder builder = Agenda.builder().defaultAgenda(true);
        return agendaRepository.save(builder.build());
    }

    public Agenda updateAgendaWeatherBaseInfo(String latLon, WeatherUrls weatherUrls) {
        Agenda defaultAgenda = retrieveDefaultAgendaCreatingIfNotPresent();
        defaultAgenda.setLatLon(latLon);
        defaultAgenda.setGeneralWeatherForecastUrl(weatherUrls.getForecastUrl());
        defaultAgenda.setHourlyWeatherForecastUrl(weatherUrls.getForecastHourlyUrl());

        return agendaRepository.save(defaultAgenda);
    }

    public AddAgendaItemStatusEnum addNewAgendaItem(String latLon, AgendaItem agendaItem) {
        Agenda agenda = agendaRepository.findByLatLon(latLon);
        if(agenda == null) {
            return AddAgendaItemStatusEnum.NO_AGENDA_WITH_LAT_LON;
        }

        var agendaItems = agenda.getAgendaItems();

        if(agendaItems == null) {
            agendaItems = new HashSet<>();
        }

        agendaItems.add(agendaItem);

        try {
            agendaRepository.save(agenda);
        } catch (Exception e) {
            log.error("Error saving agenda item", e);
            return AddAgendaItemStatusEnum.ERROR;
        }

        return AddAgendaItemStatusEnum.ADDED;
    }

    public void deleteAgendaItem(String latLon, String name) {
        agendaItemRepository.deleteAgendaItemByLatLonAndName(latLon, name);
    }
}
