package com.codenumnum.agendabyweather.dao.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Map;

@Value
public class WeatherUrls {

    String forecastUrl;
    String forecastHourlyUrl;

    @JsonCreator
    public WeatherUrls(@JsonProperty("properties") Map<String, Object> properties) {
        this.forecastUrl = (String) properties.get("forecast");
        this.forecastHourlyUrl = (String) properties.get("forecastHourly");

        if(this.forecastUrl == null || this.forecastHourlyUrl == null) {
            throw new RuntimeException("Forecast URL and Hourly URL must be provided");
        }
    }

    public String getForecastHourlyUrl() {
        return forecastHourlyUrl;
    }
}
