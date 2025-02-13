package com.codenumnum.agendabyweather.presentation;

import com.codenumnum.agendabyweather.dao.GeocodingApiDao;
import com.codenumnum.agendabyweather.dao.domain.WeatherUrls;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import com.codenumnum.agendabyweather.service.AgendaService;
import com.codenumnum.agendabyweather.service.WeatherService;
import com.codenumnum.agendabyweather.service.domain.AgendaItemCrudStatusEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/agenda-weather")
public class AgendaWeatherController {

    AgendaService agendaService;
    WeatherService weatherService;
    GeocodingApiDao geocodingApiDao;

    @GetMapping
    public Agenda getDefaultAgendaWeather() {
        Agenda defaultAgenda = agendaService.retrieveDefaultAgendaCreatingIfNotPresent();

        // If there is no latitude or longitude set yet then this must be the initial request
        // for this agenda so return to the front end as is so it knows to ask the user where
        // they want the weather from.
        if(!StringUtils.hasText(defaultAgenda.getLatLon())) {
            return defaultAgenda;
        }

        try {
            defaultAgenda.setGeneralWeatherForecast(weatherService.retrieveWeatherForecast(defaultAgenda.getGeneralWeatherForecastUrl()));
            defaultAgenda.setHourlyWeatherForecast(weatherService.retrieveWeatherForecast(defaultAgenda.getHourlyWeatherForecastUrl()));
        } catch (Exception e) {
            // Just catch but still return the agenda so the info there can be used.
            log.error("Error retrieving weather forecast. Just returning the agenda", e);
        }

        return defaultAgenda;
    }

    @PutMapping("/{address}")
    public Agenda updateLatLonOnDefaultAgenda(@PathVariable String address) {
        String latLon = geocodingApiDao.retrieveLatLonFromAddress(address);
        WeatherUrls weatherUrls = weatherService.retrieveWeatherUrls(latLon);
        Agenda agenda = agendaService.updateAgendaWeatherBaseInfo(latLon, weatherUrls);

        try {
            agenda.setGeneralWeatherForecast(weatherService.retrieveWeatherForecast(agenda.getGeneralWeatherForecastUrl()));
            agenda.setHourlyWeatherForecast(weatherService.retrieveWeatherForecast(agenda.getHourlyWeatherForecastUrl()));
        } catch (Exception e) {
            // Just catch but still return the agenda so the info there can be used.
            log.error("Error retrieving weather forecast. Just returning the agenda", e);
        }

        return agenda;
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
