package com.codenumnum.agendabyweather.presentation;

import com.codenumnum.agendabyweather.dao.GeocodingApiDao;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.codenumnum.agendabyweather.service.AgendaService;
import com.codenumnum.agendabyweather.service.domain.AgendaDto;
import com.codenumnum.agendabyweather.service.domain.AgendaItemCrudStatusEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/agenda-weather")
public class AgendaWeatherController {

    AgendaService agendaService;
    GeocodingApiDao geocodingApiDao;

    @GetMapping
    public ResponseEntity<AgendaDto> getDefaultAgendaWeather() {
        Optional<AgendaDto> defaultAgendaOptional = agendaService.retrieveDefaultAgendaDto();
        return defaultAgendaOptional
                .map(agendaDto -> {
                    // If a default agenda is found then update the weather on it, save the updates,
                    // and return the updated agenda.
                    AgendaDto updateAgendaDto = agendaService.updateGeneralAndHourlyForecastsForAgenda(agendaDto);
                    agendaService.updateAgendaEntityFromDto(updateAgendaDto);
                    return ResponseEntity.ok(updateAgendaDto);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{address}")
    public AgendaDto updateLatLonOnDefaultAgenda(@PathVariable String address) {
        String latLon = geocodingApiDao.retrieveLatLonFromAddress(address);

        AgendaDto newAgendaDto = agendaService.retrieveNewAgendaDtoForLatLon(latLon, true);
        newAgendaDto = agendaService.updateGeneralAndHourlyForecastsForAgenda(newAgendaDto);
        agendaService.saveNewAgendaFromDto(newAgendaDto);

        return newAgendaDto;
    }

    @PutMapping("/{latLon}/agenda-item")
    public AgendaItemCrudStatusEnum addAgendaItem(@PathVariable String latLon, @RequestBody AgendaItem agendaItem) {
        return agendaService.addNewAgendaItem(latLon, agendaItem);
    }

    @PutMapping("/{latLon}/agenda-item/{originalName}")
    public AgendaItemCrudStatusEnum updateAgendaItem(@PathVariable String latLon, @PathVariable String originalName, @RequestBody AgendaItem updatedAgendaItem) {
        return agendaService.updateAgendaItem(latLon, originalName, updatedAgendaItem);
    }

    @DeleteMapping("/{latLon}/agenda-item/{name}")
    public AgendaItemCrudStatusEnum deleteAgendaItem(@PathVariable String latLon, @PathVariable String name) {
        return agendaService.deleteAgendaItem(latLon, name);
    }
}
