package com.codenumnum.agendabyweather.dao.domain;

public record WeatherForecastPeriod(int number, String name, String startTime, String endTime, boolean isDaytime,
                                    float temperature, String temperatureUnit, String temperatureTrend, WeatherValueUnitCodePlaceholder probabilityOfPrecipitation,
                                    WeatherValueUnitCodePlaceholder dewpoint, WeatherValueUnitCodePlaceholder relativeHumidity, String windSpeed,
                                    String direction, String shortForecast, String longForecast) {

    public WeatherForecastPeriod archivePeriod() {
        String datePartOfString = startTime.substring(0, 10) + (isDaytime ? " Day Forecast" : " Night Forecast");

        if(datePartOfString.equals(name)) {
            return this;
        } else {
            return new WeatherForecastPeriod(number, datePartOfString, startTime, endTime, isDaytime, temperature,
                    temperatureUnit, temperatureTrend, probabilityOfPrecipitation, dewpoint, relativeHumidity,
                    windSpeed, direction, shortForecast, longForecast);
        }
    }

    public WeatherForecastPeriod updateEndDateTime(String updatedEndDateTime) {
        return new WeatherForecastPeriod(number, name, startTime, updatedEndDateTime, isDaytime, temperature,
                temperatureUnit, temperatureTrend, probabilityOfPrecipitation, dewpoint, relativeHumidity,
                windSpeed, direction, shortForecast, longForecast);
    }
}
