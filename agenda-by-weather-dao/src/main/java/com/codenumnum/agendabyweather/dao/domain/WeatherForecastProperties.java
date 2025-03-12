package com.codenumnum.agendabyweather.dao.domain;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


public record WeatherForecastProperties(String units, String generatedAt, String updateTime,
                                        List<WeatherForecastPeriod> periods) {

    public WeatherForecastProperties updateWeather(WeatherForecastProperties updatedProperties, boolean archive) {
        WeatherForecastPeriod updatedPeriod = updatedProperties.periods.get(0);
        String date2WeekAgo = subtractTwoWeeksAndFormat(updatedPeriod.startTime());
        int period2WeekAgoStartIndex = 0;
        int updatedPeriodStartIndex = this.periods.size() - 1;

        for(int i = 0; i < this.periods.size(); i++) {
            WeatherForecastPeriod period = this.periods.get(i);
            if(period.startTime().compareTo(date2WeekAgo) >= 0) {
                period2WeekAgoStartIndex = i;
                break;
            }
        }

        // Count backwards since updates should happen regularly by a scheduled job and will be
        // shorter than the 2 weeks saved.
        for(int i = updatedPeriodStartIndex; i >= 0; i--) {
            WeatherForecastPeriod period = this.periods.get(i);
            if(period.startTime().compareTo(updatedPeriod.startTime()) <= 0) {
                updatedPeriodStartIndex = i;
                break;
            }
        }

        List<WeatherForecastPeriod> updatedPeriods = this.periods.subList(period2WeekAgoStartIndex, updatedPeriodStartIndex);
        if(archive) {
            updatedPeriods = updatedPeriods.stream().map(WeatherForecastPeriod::archivePeriod).collect(Collectors.toList());
        }
        updatedPeriods.addAll(updatedProperties.periods);

        return new WeatherForecastProperties(updatedProperties.units, updatedProperties.generatedAt, updatedProperties.updateTime, updatedPeriods);
    }

    private static String subtractTwoWeeksAndFormat(String dateString) {
        // Define the formatter for the given date format
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        // Parse the string into an OffsetDateTime object
        OffsetDateTime dateTime = OffsetDateTime.parse(dateString, formatter);

        // Subtract two weeks
        OffsetDateTime updatedDateTime = dateTime.minusWeeks(2);

        // Return the updated date as a string in the same format
        return updatedDateTime.format(formatter);
    }
}
