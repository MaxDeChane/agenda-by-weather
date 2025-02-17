package com.codenumnum.agendabyweather.presentation;

import com.codenumnum.agendabyweather.dao.GeocodingApiDao;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.codenumnum.agendabyweather.service.AgendaService;
import com.codenumnum.agendabyweather.service.domain.AgendaItemCrudStatusEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/agenda-weather")
public class AgendaWeatherController {

    AgendaService agendaService;
    GeocodingApiDao geocodingApiDao;

    @GetMapping
    public Agenda getDefaultAgendaWeather() {
        return agendaService.retrieveDefaultAgendaCreatingIfNotPresent();
    }

    @PutMapping("/{address}")
    public Agenda updateLatLonOnDefaultAgenda(@PathVariable String address) {
        String latLon = geocodingApiDao.retrieveLatLonFromAddress(address);

        return agendaService.updateAgendaWeatherBaseInfo(latLon);
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
