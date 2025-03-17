package com.codenumnum.agendabyweather.service.domain.factory;

import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDay;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.codenumnum.agendabyweather.service.domain.AgendaDayDto;
import com.codenumnum.agendabyweather.service.domain.AgendaDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class AgendaFactory {

    AgendaDayFactory agendaDayFactory;

    public AgendaDto createDtoFromExistingAgenda(Agenda agenda) {
        Map<LocalDate, AgendaDayDto> agendaDaysByDateString = new HashMap<>();
        if(!CollectionUtils.isEmpty(agenda.getAgendaDays())) {
            for (AgendaDay agendaDay : agenda.getAgendaDays()) {
                AgendaDayDto agendaDayDto = agendaDayFactory.createDtoFromEntity(agendaDay);
                agendaDaysByDateString.put(agendaDayDto.dayDate(), agendaDayDto);
            }
        }

        List<AgendaItem> sortedAgendaItems = agenda.getAgendaItems().stream().sorted().toList();

        return new AgendaDto(agenda.getLatLon(), agenda.isDefaultAgenda(), null, agenda.getHourlyWeatherForecastUrl(),
                agenda.getHourlyWeatherGeneratedAt(), agenda.getHourlyWeatherUpdateTime(), agenda.getGeneralWeatherForecastUrl(),
                agenda.getGeneralWeatherGeneratedAt(), agenda.getGeneralWeatherUpdateTime(), sortedAgendaItems, agendaDaysByDateString);
    }

    public AgendaDto createDtoFromDtoWithGeneratedAndUpdatedWeatherTimestamps(AgendaDto agendaDto, OffsetDateTime generatedAt,
                                                                              OffsetDateTime updatedAt, ZoneOffset offset, boolean isGeneralForecast) {
        if (isGeneralForecast) {
            return new AgendaDto(agendaDto.latLon(), agendaDto.defaultAgenda(), offset, agendaDto.hourlyWeatherForecastUrl(),
                    agendaDto.hourlyWeatherGeneratedAt(), agendaDto.hourlyWeatherUpdateTime(), agendaDto.generalWeatherForecastUrl(),
                    generatedAt, updatedAt, agendaDto.agendaItems(), agendaDto.agendaDaysByDay());
        }

        return new AgendaDto(agendaDto.latLon(), agendaDto.defaultAgenda(), offset, agendaDto.hourlyWeatherForecastUrl(),
                generatedAt, updatedAt, agendaDto.generalWeatherForecastUrl(), agendaDto.generalWeatherGeneratedAt(), agendaDto.generalWeatherUpdateTime(),
                agendaDto.agendaItems(), agendaDto.agendaDaysByDay());
    }

    public Agenda createNewEntityFromDto(AgendaDto agendaDto) {
        // Build the agenda before adding the agenda days since the id
        // of the agenda is needed to create the days.
        Agenda agendaEntity = Agenda.builder()
                .latLon(agendaDto.latLon())
                .defaultAgenda(agendaDto.defaultAgenda())
                .generalWeatherForecastUrl(agendaDto.generalWeatherForecastUrl())
                .generalWeatherGeneratedAt(agendaDto.generalWeatherGeneratedAt())
                .generalWeatherUpdateTime(agendaDto.generalWeatherUpdateTime())
                .hourlyWeatherForecastUrl(agendaDto.hourlyWeatherForecastUrl())
                .hourlyWeatherGeneratedAt(agendaDto.hourlyWeatherGeneratedAt())
                .hourlyWeatherUpdateTime(agendaDto.hourlyWeatherUpdateTime())
                .agendaItems(new HashSet<>(agendaDto.agendaItems()))
                .build();

        Set<AgendaDay> agendaDayEntities =
                agendaDto.agendaDaysByDay().values().stream()
                        .map(agendaDayDto -> agendaDayFactory.createEntityFromDto(agendaDayDto, agendaDto.offset(), agendaEntity.getId()))
                        .collect(Collectors.toSet());

        agendaEntity.setAgendaDays(agendaDayEntities);

        return agendaEntity;
    }
}
