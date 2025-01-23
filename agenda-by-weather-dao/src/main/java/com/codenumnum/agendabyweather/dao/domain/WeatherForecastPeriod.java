package com.codenumnum.agendabyweather.dao.domain;

public record WeatherForecastPeriod(int number, String name, String startTime, String endTime, boolean isDaytime,
                                    float temperature, String temperatureUnit, String temperatureTrend, WeatherValueUnitCodePlaceholder probabilityOfPrecipitation,
                                    WeatherValueUnitCodePlaceholder dewpoint, WeatherValueUnitCodePlaceholder relativeHumidity, String windSpeed,
                                    String direction, String shortForecast, String longForecast) {
}
