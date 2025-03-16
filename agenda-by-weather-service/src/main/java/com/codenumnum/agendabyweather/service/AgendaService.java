package com.codenumnum.agendabyweather.service;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import com.codenumnum.agendabyweather.dao.domain.WeatherForecastPeriod;
import com.codenumnum.agendabyweather.dao.domain.WeatherForecastProperties;
import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.codenumnum.agendabyweather.dao.repository.AgendaItemRepository;
import com.codenumnum.agendabyweather.dao.repository.AgendaRepository;
import com.codenumnum.agendabyweather.service.domain.AgendaDayDto;
import com.codenumnum.agendabyweather.service.domain.AgendaDto;
import com.codenumnum.agendabyweather.service.domain.AgendaItemCrudStatusEnum;
import com.codenumnum.agendabyweather.service.domain.factory.AgendaDayDtoFactory;
import com.codenumnum.agendabyweather.service.domain.factory.AgendaDtoFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class AgendaService {

    AgendaRepository agendaRepository;
    AgendaDtoFactory agendaDtoFactory;
    AgendaDayDtoFactory agendaDayDtoFactory;
    AgendaItemRepository agendaItemRepository;
    WeatherService weatherService;
    DateTimeService dateTimeService;
    ObjectMapper objectMapper;

    public AgendaDto retrieveDefaultAgendaCreatingIfNotPresent(Optional<WeatherUrls> weatherUrlsOptional) {
        var agendaOptional = agendaRepository.findByDefaultAgenda(true);

        AgendaDto defaultAgendaDto;
        if (agendaOptional.isPresent()) {
            log.info("Found default agenda. Update weather.");
            defaultAgendaDto = agendaDtoFactory.createFromExistingAgenda(agendaOptional.get(), objectMapper);
            try {
                String generalWeatherUrl;
                String hourlyWeatherUrl;
                if(weatherUrlsOptional.isPresent()) {
                    generalWeatherUrl = weatherUrlsOptional.get().getForecastUrl();
                    hourlyWeatherUrl = weatherUrlsOptional.get().getForecastHourlyUrl();
                } else {
                    generalWeatherUrl = defaultAgendaDto.getGeneralWeatherForecastUrl();
                    hourlyWeatherUrl = defaultAgendaDto.getHourlyWeatherForecastUrl();
                }

                updateWeatherOnAgenda(defaultAgendaDto, weatherService.retrieveWeatherForecast(generalWeatherUrl), true);
                updateWeatherOnAgenda(defaultAgendaDto, weatherService.retrieveWeatherForecast(hourlyWeatherUrl), false);
            } catch (Exception e) {
                // Just catch but still return the agenda so the info there can be used.
                log.error("Error retrieving weather forecast. Just returning the agenda", e);
            }
        } else {
            log.info("Default agenda not found so creating new one");
            // Initial will have a value of empty so the front end knows to get
            // the info from the user.
            defaultAgendaDto = new AgendaDto(Agenda.builder().defaultAgenda(true).build(), Collections.emptyMap());
        }

        agendaRepository.save(defaultAgendaDto.agenda());

        return defaultAgendaDto;
    }

    /**
     * This method doesn't save the agenda to the db since other operations may be needed
     * also. Instead, that will be left up to the calling methods to decide if and when
     * changes need to be saved to the backing Agenda.
     *
     * @param agendaDto
     * @param weatherForecast
     * @param isGeneralForecast
     */
    public void updateWeatherOnAgenda(AgendaDto agendaDto, WeatherForecast weatherForecast, boolean isGeneralForecast) {
        WeatherForecastProperties weatherForecastProperties = weatherForecast.properties();
        OffsetDateTime generatedAt;
        OffsetDateTime updatedAt;

        if(isGeneralForecast) {
            generatedAt = agendaDto.getGeneralWeatherGeneratedAt();
            updatedAt = agendaDto.getGeneralWeatherUpdateTime();
        } else {
            generatedAt = agendaDto.getHourlyWeatherGeneratedAt();
            updatedAt = agendaDto.getHourlyWeatherUpdateTime();
        }

//        if(generatedAt != null && updatedAt != null &&
//                generatedAt.equals(weatherForecastProperties.generatedAt()) &&
//                updatedAt.equals(weatherForecastProperties.updateTime())) {
//            log.info("No update for weather needed.");
//            return;
//        }

        Map<LocalDate, Map<OffsetDateTime, WeatherForecastPeriod>> updatedWeatherPeriodsByDay = weatherService.mapWeatherPeriodsByDay(weatherForecastProperties.periods());

        LocalDate firstUpdatedDay = null;
        for(Map.Entry<LocalDate, Map<OffsetDateTime, WeatherForecastPeriod>> entry : updatedWeatherPeriodsByDay.entrySet()) {
            // Get the first
            if(firstUpdatedDay == null || entry.getKey().isBefore(firstUpdatedDay)) {
                firstUpdatedDay = entry.getKey();
            }

            AgendaDayDto agendaDayDto = agendaDto.agendaDaysByDay().get(entry.getKey());
            if(agendaDayDto == null) {
                agendaDayDto = agendaDayDtoFactory.createNewWithWeatherForecast(agendaDto.agenda(), entry.getKey(), entry.getValue(), isGeneralForecast, objectMapper);
            } else {
                agendaDayDto = agendaDayDto.updateForecast(entry.getValue(), isGeneralForecast, objectMapper);
            }

            // Update/Add the day to the backing agenda days collection to be saved to the
            // database.
            agendaDto.getAgendaDays().add(agendaDayDto.agendaDay());
            // Add to map for easy look up and retrieval.
            agendaDto.agendaDaysByDay().put(entry.getKey(), agendaDayDto);
        }

        if(isGeneralForecast) {
            agendaDto.runArchivalProcess(firstUpdatedDay, objectMapper);

            // Update this as the last thing so if something fails before here it will be
            // tried again.
            agendaDto.setGeneralWeatherGeneratedAt(weatherForecastProperties.generatedAt());
            agendaDto.setGeneralWeatherUpdateTime(weatherForecastProperties.updateTime());
        } else {
            // Update this as the last thing so if something fails before here it will be
            // tried again.
            agendaDto.setHourlyWeatherGeneratedAt(weatherForecastProperties.generatedAt());
            agendaDto.setHourlyWeatherUpdateTime(weatherForecastProperties.updateTime());
        }
    }

    public AgendaDto updateAgendaWeatherBaseInfo(String latLon) {
        WeatherUrls weatherUrls = weatherService.retrieveWeatherUrls(latLon);
        AgendaDto defaultAgendaDto = retrieveDefaultAgendaCreatingIfNotPresent(Optional.of(weatherUrls));
        defaultAgendaDto.setLatLon(latLon);
        defaultAgendaDto.setGeneralWeatherForecastUrl(weatherUrls.getForecastUrl());
        defaultAgendaDto.setHourlyWeatherForecastUrl(weatherUrls.getForecastHourlyUrl());

        agendaRepository.save(defaultAgendaDto.agenda());

        return defaultAgendaDto;
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
            agendaItems = new ArrayList<>();
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
