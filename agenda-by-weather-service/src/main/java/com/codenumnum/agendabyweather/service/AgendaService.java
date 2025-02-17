package com.codenumnum.agendabyweather.service;

import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.codenumnum.agendabyweather.dao.repository.AgendaItemRepository;
import com.codenumnum.agendabyweather.dao.repository.AgendaRepository;
import com.codenumnum.agendabyweather.service.domain.AgendaItemCrudStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashSet;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class AgendaService {

    AgendaRepository agendaRepository;
    AgendaItemRepository agendaItemRepository;
    WeatherService weatherService;
    ObjectMapper objectMapper;

    public Agenda retrieveDefaultAgendaCreatingIfNotPresent() {
        var agendaOptional = agendaRepository.findByDefaultAgenda(true);

        Agenda defaultAgenda;
        if (agendaOptional.isPresent()) {
            log.info("Found default agenda. Update weather.");
            defaultAgenda = agendaOptional.get();
            try {
                defaultAgenda.updateWeatherForecasts(weatherService.retrieveWeatherForecast(defaultAgenda.getGeneralWeatherForecastUrl()),
                        weatherService.retrieveWeatherForecast(defaultAgenda.getHourlyWeatherForecastUrl()), objectMapper);
            } catch (Exception e) {
                // Just catch but still return the agenda so the info there can be used.
                log.error("Error retrieving weather forecast. Just returning the agenda", e);
            }
        } else {
            log.info("Default agenda not found so creating new one");
            // Initial will have a value of empty so the front end knows to get
            // the info from the user.
            defaultAgenda = Agenda.builder().defaultAgenda(true).build();
        }


        return agendaRepository.save(defaultAgenda);
    }

    public Agenda updateAgendaWeatherBaseInfo(String latLon) {
        WeatherUrls weatherUrls = weatherService.retrieveWeatherUrls(latLon);
        Agenda defaultAgenda = retrieveDefaultAgendaCreatingIfNotPresent();
        defaultAgenda.setLatLon(latLon);
        defaultAgenda.setGeneralWeatherForecastUrl(weatherUrls.getForecastUrl());
        defaultAgenda.setHourlyWeatherForecastUrl(weatherUrls.getForecastHourlyUrl());

        return agendaRepository.save(defaultAgenda);
    }

    public AgendaItemCrudStatusEnum addNewAgendaItem(String latLon, AgendaItem agendaItem) {
        Agenda agenda = agendaRepository.findByLatLon(latLon);
        if(agenda == null) {
            return AgendaItemCrudStatusEnum.NO_AGENDA_WITH_LAT_LON;
        }

        if(!StringUtils.hasText(agendaItem.getName())) {
            agendaItem.setName("Untitled_" + Instant.now());
        }

        var agendaItems = agenda.getAgendaItems();

        if(agendaItems == null) {
            agendaItems = new HashSet<>();
        } else if(agendaItems.contains(agendaItem)) {
            return AgendaItemCrudStatusEnum.ALREADY_EXISTS;
        }

        agendaItems.add(agendaItem);

        try {
            agendaRepository.save(agenda);
        } catch (Exception e) {
            log.error("Error saving agenda item", e);
            return AgendaItemCrudStatusEnum.ERROR;
        }

        return AgendaItemCrudStatusEnum.ADDED;
    }

    //TODO latLon will be used for future functionality
    public AgendaItemCrudStatusEnum updateAgendaItem(String latLon, String originalName, AgendaItem updatedAgendaItem) {

        var existingAgendaItem = agendaItemRepository.findByName(originalName);

        if(existingAgendaItem.isEmpty()) {
            return AgendaItemCrudStatusEnum.NO_AGENDA_ITEM_WITH_NAME;
        }

        existingAgendaItem.get().performFullAgendaTransfer(updatedAgendaItem);

        try {
            agendaItemRepository.save(existingAgendaItem.get());
        } catch (Exception e) {
            log.error("Error updating agenda item with original name {}", originalName, e);
            return AgendaItemCrudStatusEnum.ERROR;
        }

        return AgendaItemCrudStatusEnum.UPDATED;
    }

    public AgendaItemCrudStatusEnum deleteAgendaItem(String latLon, String name) {
        try {
            agendaItemRepository.deleteAgendaItemsEntry(latLon, name);
            agendaItemRepository.deleteAgendaItemByName(name);
            return AgendaItemCrudStatusEnum.DELETED;
        } catch (Exception e) {
            log.error("Error deleting agenda item with name {}", name, e);
            return AgendaItemCrudStatusEnum.ERROR;
        }
    }
}
