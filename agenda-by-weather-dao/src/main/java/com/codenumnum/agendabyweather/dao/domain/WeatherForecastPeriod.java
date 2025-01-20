package com.codenumnum.agendabyweather.dao.domain;

import java.time.Instant;

public record WeatherForecastPeriod(int number, String name, Instant startTime, Instant endTime, boolean isDaytime,
                                    float temperature, String temperatureUnit, String temperatureTrend, WeatherValueUnitCodePlaceholder probabilityOfPrecipitation,
                                    WeatherValueUnitCodePlaceholder dewpoint, WeatherValueUnitCodePlaceholder relativeHumidity,String windSpeed,
                                    String direction, String shortForecast, String longForecast) {
}
