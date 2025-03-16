package com.codenumnum.agendabyweather.service.domain;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecastPeriod;
import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDay;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDayKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public record AgendaDayDto(
        @Delegate @JsonIgnore AgendaDay agendaDay,
        Map<OffsetDateTime, WeatherForecastPeriod> generalWeatherPeriods,
        Map<OffsetDateTime, WeatherForecastPeriod> hourlyWeatherPeriods) {

    @SneakyThrows
    public AgendaDayDto updateForecast(Map<OffsetDateTime, WeatherForecastPeriod> updatedPeriodsByStartTime, boolean isGeneralForecast,
                                       ObjectMapper objectMapper) {
        Map<OffsetDateTime, WeatherForecastPeriod> currentForecastPeriods = (isGeneralForecast) ? this.generalWeatherPeriods : this.hourlyWeatherPeriods;

        if(CollectionUtils.isEmpty(currentForecastPeriods)) {
            String forecastPeriodsByStartTimeJson = objectMapper.writeValueAsString(updatedPeriodsByStartTime);
            if(isGeneralForecast) {
                agendaDay.setGeneralWeatherForecastJson(forecastPeriodsByStartTimeJson);
                return new AgendaDayDto(agendaDay, updatedPeriodsByStartTime, this.hourlyWeatherPeriods);
            }
            agendaDay.setHourlyWeatherForecastJson(forecastPeriodsByStartTimeJson);
            return new AgendaDayDto(agendaDay, this.generalWeatherPeriods, updatedPeriodsByStartTime);
        }

        // Replace any current periods with updated ones.
        updatedPeriodsByStartTime.keySet().stream()
                .sorted()
                .forEach(updatedPeriodKey -> {
                    if (currentForecastPeriods.containsKey(updatedPeriodKey)) {
                        currentForecastPeriods.put(updatedPeriodKey, updatedPeriodsByStartTime.get(updatedPeriodKey));
                    } else {
                        Optional<OffsetDateTime> keysOfPeriodsToUpdate =
                                currentForecastPeriods.entrySet().stream()
                                        .filter(currentPeriodEntry -> {
                                            WeatherForecastPeriod currentPeriod = currentPeriodEntry.getValue();

                                            // If the updated period starts within a current period time frame,
                                            // this will be considered the updated replacement for that time frame.
                                            // Really only pertains to general forecast since they can spread multiple
                                            // hours.
                                            return (currentPeriod.startTime().isBefore(updatedPeriodKey) || currentPeriod.startTime().isEqual(updatedPeriodKey)) &&
                                                    (currentPeriod.endTime().isAfter(updatedPeriodKey) || currentPeriod.endTime().isEqual(updatedPeriodKey));
                                        })
                                        .map(Map.Entry::getKey)
                                        .findAny();

                        // Remove any conflicting periods since that overlap will
                        keysOfPeriodsToUpdate.ifPresent(currentForecastPeriods::remove);
                        currentForecastPeriods.put(updatedPeriodKey, updatedPeriodsByStartTime.get(updatedPeriodKey));
                    }
                });

        String forecastPeriodsByStartTimeJson = objectMapper.writeValueAsString(currentForecastPeriods);
        if(isGeneralForecast) {
            agendaDay.setGeneralWeatherForecastJson(forecastPeriodsByStartTimeJson);
        } else {
            agendaDay.setHourlyWeatherForecastJson(forecastPeriodsByStartTimeJson);
        }

        return this;
    }

    /**
     * The archival process goes through and
     * @return true if archival was done; otherwise, false.
     */
    @SneakyThrows
    public boolean archiveGeneralWeatherPeriods(ObjectMapper objectMapper) {
        if(!CollectionUtils.isEmpty(generalWeatherPeriods)) {
            Map<OffsetDateTime, WeatherForecastPeriod> updatedPeriods = generalWeatherPeriods.entrySet().stream()
                    .map((entry) -> Map.entry(entry.getKey(), entry.getValue().archiveGeneralWeatherPeriod()))
                    // Ensure we only collect those that have been updated. This can be done by checking if the reference has changed;
                    // if it hasn't, the object is the same and hasn't been updated, since the period record is immutable.
                    .filter(entry -> entry.getValue() != generalWeatherPeriods.get(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if(CollectionUtils.isEmpty(updatedPeriods)) {
                return false;
            }

            generalWeatherPeriods.putAll(updatedPeriods);
            agendaDay.setGeneralWeatherForecastJson(objectMapper.writeValueAsString(generalWeatherPeriods));

            return true;
        }

        return false;
    }

    @JsonIgnore
    public AgendaDayKey getAgendaDayKey() {
        return agendaDay.getAgendaDayKey();
    }

    @JsonIgnore
    public String getGeneralWeatherForecastJson() {
        return agendaDay.getGeneralWeatherForecastJson();
    }

    @JsonIgnore
    public String getHourlyWeatherForecastJson() {
        return agendaDay.getHourlyWeatherForecastJson();
    }
}
