package com.codenumnum.agendabyweather.service;

import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.codenumnum.agendabyweather.dao.repository.AgendaItemRepository;
import com.codenumnum.agendabyweather.dao.repository.AgendaRepository;
import com.codenumnum.agendabyweather.service.domain.AgendaItemCrudStatusEnum;
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
            log.error("Error saving agenda item", e);
            return AgendaItemCrudStatusEnum.ERROR;
        }

        return AgendaItemCrudStatusEnum.UPDATED;
    }

    public void deleteAgendaItem(String latLon, String name) {
        agendaItemRepository.deleteAgendaItemsEntry(latLon, name);
        agendaItemRepository.deleteAgendaItemByName(name);
    }
}
