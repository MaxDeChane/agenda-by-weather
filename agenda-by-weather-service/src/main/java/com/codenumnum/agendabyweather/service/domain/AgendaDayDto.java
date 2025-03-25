package com.codenumnum.agendabyweather.service.domain;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecastPeriod;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDay;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaDayKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public record AgendaDayDto(
        LocalDate dayDate,
        Map<OffsetDateTime, WeatherForecastPeriod> generalWeatherPeriods,
        Map<OffsetDateTime, WeatherForecastPeriod> hourlyWeatherPeriods) {

    @SneakyThrows
    public AgendaDayDto updateForecast(Map<OffsetDateTime, WeatherForecastPeriod> updatedPeriodsByStartTime, boolean isGeneralForecast) {
        Map<OffsetDateTime, WeatherForecastPeriod> currentForecastPeriods = (isGeneralForecast) ? this.generalWeatherPeriods : this.hourlyWeatherPeriods;

        if(CollectionUtils.isEmpty(currentForecastPeriods)) {
            LocalDate dayDateOfPeriods = updatedPeriodsByStartTime.keySet().iterator().next().toLocalDate();
            if(isGeneralForecast) {
                return new AgendaDayDto(dayDateOfPeriods, updatedPeriodsByStartTime, this.hourlyWeatherPeriods);
            }

            return new AgendaDayDto(dayDateOfPeriods, this.generalWeatherPeriods, updatedPeriodsByStartTime);
        }

        // Replace any current periods with updated ones.
        updatedPeriodsByStartTime.keySet().stream()
                .sorted()
                .forEach(updatedPeriodKey -> {
                    if (currentForecastPeriods.containsKey(updatedPeriodKey)) {
                        currentForecastPeriods.put(updatedPeriodKey, updatedPeriodsByStartTime.get(updatedPeriodKey));
                    } else {
                        Optional<OffsetDateTime> keyOfPeriodsToUpdate =
                                currentForecastPeriods.entrySet().stream()
                                        .filter(currentPeriodEntry -> {
                                            WeatherForecastPeriod currentPeriod = currentPeriodEntry.getValue();

                                            // If the updated period starts within a current period time frame,
                                            // this will be considered the updated replacement for that time frame.
                                            // Really only pertains to general forecast since they can spread multiple
                                            // hours.
                                            return (currentPeriod.startTime().isBefore(updatedPeriodKey) || currentPeriod.startTime().isEqual(updatedPeriodKey)) &&
                                                    (currentPeriod.endTime().isAfter(updatedPeriodKey));
                                        })
                                        .map(Map.Entry::getKey)
                                        .findAny();

                        // Remove any conflicting periods since that overlap will
                        keyOfPeriodsToUpdate.ifPresent(currentForecastPeriods::remove);
                        currentForecastPeriods.put(updatedPeriodKey, updatedPeriodsByStartTime.get(updatedPeriodKey));
                    }
                });

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

            return true;
        }

        return false;
    }
}
