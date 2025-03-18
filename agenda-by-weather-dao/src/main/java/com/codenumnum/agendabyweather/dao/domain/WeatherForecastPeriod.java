package com.codenumnum.agendabyweather.dao.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public record WeatherForecastPeriod(int number, String name, OffsetDateTime startTime, OffsetDateTime endTime, boolean isDaytime,
                                    float temperature, String temperatureUnit, String temperatureTrend, WeatherValueUnitCodePlaceholder probabilityOfPrecipitation,
                                    WeatherValueUnitCodePlaceholder dewpoint, WeatherValueUnitCodePlaceholder relativeHumidity, String windSpeed,
                                    String direction, String shortForecast, String longForecast) {

    public WeatherForecastPeriod archiveGeneralWeatherPeriod() {
        LocalDate periodDateEndTimeDate = endTime.toLocalDate();
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        LocalDate today = currentDateTime.toLocalDate();
        LocalDate yesterday = today.minusDays(1);

        boolean isEarlierTodayOrLastNight = periodDateEndTimeDate.equals(today) && startTime.isBefore(currentDateTime) &&
                endTime.isBefore(currentDateTime) && (endTime.toLocalTime().isBefore(LocalTime.parse("18:00")));

        String updatedName;

        if (isEarlierTodayOrLastNight) {
            updatedName = isDaytime ? "Earlier Today" : "Last Night";
        } else if (isDaytime && periodDateEndTimeDate.equals(yesterday)) {
            updatedName = "Yesterday";
        } else if (periodDateEndTimeDate.isBefore(yesterday)) {  // Covers anything older than yesterday
            updatedName = periodDateEndTimeDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + (isDaytime ? " Day" : " Night");
        } else {
            updatedName = name;
        }

        if (updatedName.equals(name)) {
            return this;
        } else {
            return new WeatherForecastPeriod(number, updatedName, startTime, endTime, isDaytime, temperature,
                    temperatureUnit, temperatureTrend, probabilityOfPrecipitation, dewpoint, relativeHumidity,
                    windSpeed, direction, shortForecast, longForecast);
        }
    }

}
