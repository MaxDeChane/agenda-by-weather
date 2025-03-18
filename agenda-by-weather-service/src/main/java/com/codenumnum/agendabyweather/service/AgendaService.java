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
import com.codenumnum.agendabyweather.service.domain.factory.AgendaDayFactory;
import com.codenumnum.agendabyweather.service.domain.factory.AgendaFactory;
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
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class AgendaService {

    AgendaRepository agendaRepository;
    AgendaFactory agendaFactory;
    AgendaDayFactory agendaDayFactory;
    AgendaItemRepository agendaItemRepository;
    WeatherService weatherService;
    ObjectMapper objectMapper;

    public Optional<AgendaDto> retrieveDefaultAgendaDto() {
        Optional<Agenda> defaultAgendaOptional = agendaRepository.findByDefaultAgenda(true);

        return defaultAgendaOptional.map(agendaFactory::createDtoFromExistingAgenda);

    }

    public AgendaDto updateGeneralAndHourlyForecastsForAgenda(AgendaDto agendaDto) {
        try {
            AgendaDto updatedAgendaDto = updateGeneralOrHourlyForecastForAgenda(agendaDto, weatherService.retrieveWeatherForecast(agendaDto.generalWeatherForecastUrl()), true);
            updatedAgendaDto = updateGeneralOrHourlyForecastForAgenda(updatedAgendaDto, weatherService.retrieveWeatherForecast(agendaDto.hourlyWeatherForecastUrl()), false);

            return updatedAgendaDto;
        } catch (Exception e) {
            // Just catch but still return the agenda so the info there can be used.
            log.error("Error retrieving weather forecast. Just returning the agenda", e);

            return agendaDto;
        }
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
    public AgendaDto updateGeneralOrHourlyForecastForAgenda(AgendaDto agendaDto, WeatherForecast weatherForecast, boolean isGeneralForecast) {
        WeatherForecastProperties weatherForecastProperties = weatherForecast.properties();
        OffsetDateTime generatedAt;
        OffsetDateTime updatedAt;

        if(isGeneralForecast) {
            generatedAt = agendaDto.generalWeatherGeneratedAt();
            updatedAt = agendaDto.generalWeatherUpdateTime();
        } else {
            generatedAt = agendaDto.hourlyWeatherGeneratedAt();
            updatedAt = agendaDto.hourlyWeatherUpdateTime();
        }

//        if(generatedAt != null && updatedAt != null &&
//                generatedAt.equals(weatherForecastProperties.generatedAt()) &&
//                updatedAt.equals(weatherForecastProperties.updateTime())) {
//            log.info("No update for weather needed.");
//            return;
//        }

        Map<LocalDate, Map<OffsetDateTime, WeatherForecastPeriod>> updatedWeatherPeriodsByDay = weatherService.mapWeatherPeriodsByDay(weatherForecastProperties.periods());

        LocalDate firstUpdatedDay = null;
        ZoneOffset offset = ZoneOffset.UTC;
        for(Map.Entry<LocalDate, Map<OffsetDateTime, WeatherForecastPeriod>> entry : updatedWeatherPeriodsByDay.entrySet()) {
            // Get the first
            if(firstUpdatedDay == null || entry.getKey().isBefore(firstUpdatedDay)) {
                firstUpdatedDay = entry.getKey();
                offset = entry.getValue().values().iterator().next().startTime().getOffset();
            }

            AgendaDayDto agendaDayDto = agendaDto.agendaDaysByDay().get(entry.getKey());
            if(agendaDayDto == null) {
                agendaDayDto = agendaDayFactory.createNewDtoWithWeatherForecast(entry.getKey(), entry.getValue(), isGeneralForecast);
            } else {
                agendaDayDto = agendaDayDto.updateForecast(entry.getValue(), isGeneralForecast);
            }

            // Add to map for easy look up and retrieval.
            agendaDto.agendaDaysByDay().put(entry.getKey(), agendaDayDto);
        }

        agendaDto = agendaFactory.createDtoFromDtoWithGeneratedAndUpdatedWeatherTimestamps(agendaDto, generatedAt, updatedAt,
                offset, isGeneralForecast);

        if(isGeneralForecast) {
            agendaDto.runArchivalProcess(firstUpdatedDay, objectMapper);
        }

        return agendaDto;
    }

    public AgendaDto retrieveNewAgendaDtoForLatLon(String latLon, boolean isDefaultAgendaDto) {
        WeatherUrls weatherUrls = weatherService.retrieveWeatherUrls(latLon);

        return new AgendaDto(latLon, isDefaultAgendaDto, null, weatherUrls.getForecastHourlyUrl(), null, null,
                weatherUrls.getForecastUrl(), null, null, new ArrayList<>(), new HashMap<>());
    }

    public boolean saveNewAgendaFromDto(AgendaDto agendaDto) {
        Agenda newAgendaEntity = agendaFactory.createNewEntityFromDto(agendaDto);

        try {
            agendaRepository.save(newAgendaEntity);
            return true;
        } catch (Exception e) {
            // TODO add retry logic when time permits.
            log.error("Error saving new agenda.", e);
            return false;
        }
    }

    public boolean updateAgendaEntityFromDto(AgendaDto agendaDto) {
        Optional<Agenda> agendaOptional = agendaRepository.findByLatLon(agendaDto.latLon());

        if(agendaOptional.isEmpty()) {
            log.error("Agenda with lat/lon not found when should exist");
            return false;
        }

        Agenda agenda = agendaOptional.get();

        agenda.setGeneralWeatherGeneratedAt(agendaDto.generalWeatherGeneratedAt());
        agenda.setGeneralWeatherUpdateTime(agendaDto.generalWeatherUpdateTime());
        agenda.setHourlyWeatherGeneratedAt(agendaDto.hourlyWeatherGeneratedAt());
        agenda.setHourlyWeatherUpdateTime(agendaDto.hourlyWeatherUpdateTime());

        agenda.getAgendaItems().addAll(agendaDto.agendaItems());

        agenda.getAgendaDays().addAll(agendaDto.agendaDaysByDay().values().stream()
                .map(agendaDayDto -> agendaDayFactory.createEntityFromDto(agendaDayDto, agendaDto.offset(), agenda.getId()))
                .collect(Collectors.toSet()));

        try {
            agendaRepository.save(agenda);
            return true;
        } catch (Exception e) {
            // TODO add retry logic when time permits.
            log.error("Error saving new agenda.", e);
            return false;
        }
    }

    public AgendaItemCrudStatusEnum addNewAgendaItem(String latLon, AgendaItem agendaItem) {
        Optional<Agenda> agendaOptional = agendaRepository.findByLatLon(latLon);
        if(agendaOptional.isEmpty()) {
            return AgendaItemCrudStatusEnum.NO_AGENDA_WITH_LAT_LON;
        }

        Agenda agenda = agendaOptional.get();

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
