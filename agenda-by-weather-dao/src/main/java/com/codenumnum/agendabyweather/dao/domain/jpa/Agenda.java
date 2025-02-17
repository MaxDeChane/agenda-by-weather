package com.codenumnum.agendabyweather.dao.domain.jpa;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Log4j2
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Agenda {

    @Id
    @Builder.Default
    UUID id = UUID.randomUUID();
    @Column(unique=true)
    @Builder.Default
    String latLon = "";
    @Column(nullable = false)
    boolean defaultAgenda;
    String hourlyWeatherForecastUrl;
    String generalWeatherForecastUrl;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Set<AgendaItem> agendaItems;
    /*Don't save the actual weather forecast since it will be stored
     as just a json string in the db. If we want to filter based on any
     fields in the forecast they will be extracted out to their own columns
     but most filtering besides dates and locations will be done in code and
     not in the db.*/
    @Transient
    WeatherForecast hourlyWeatherForecast;
    @JsonIgnore
    @Lob
    String hourlyWeatherForecastJson;
    @Transient
    WeatherForecast generalWeatherForecast;
    @JsonIgnore
    @Lob
    String generalWeatherForecastJson;

    @SneakyThrows
    public void updateWeatherForecasts(WeatherForecast generalWeatherForecast, WeatherForecast hourlyWeatherForecast,
                                          ObjectMapper objectMapper) {
        if(generalWeatherForecast != null) {
            if (StringUtils.hasText(this.generalWeatherForecastJson)) {
                var forecastFromJson = objectMapper.readValue(this.generalWeatherForecastJson, WeatherForecast.class);
                var oldProperties = forecastFromJson.properties();
                var newProperties = generalWeatherForecast.properties();
                if (oldProperties.weatherNeedsUpdate(newProperties)) {
                    var combinedProperties = oldProperties.updateWeather(newProperties);
                    this.generalWeatherForecast = new WeatherForecast(combinedProperties);
                    this.generalWeatherForecastJson = objectMapper.writeValueAsString(generalWeatherForecast);
                }
            } else {
                log.info("No weather forecast json found so just setting it new.");
                this.generalWeatherForecast = generalWeatherForecast;
                this.generalWeatherForecastJson = objectMapper.writeValueAsString(generalWeatherForecast);
            }
        }

        if(hourlyWeatherForecast != null) {
            if (StringUtils.hasText(this.hourlyWeatherForecastJson)) {
                var forecastFromJson = objectMapper.readValue(this.hourlyWeatherForecastJson, WeatherForecast.class);
                var oldProperties = forecastFromJson.properties();
                var newProperties = hourlyWeatherForecast.properties();
                if (oldProperties.weatherNeedsUpdate(newProperties)) {
                    var combinedProperties = oldProperties.updateWeather(newProperties);
                    this.hourlyWeatherForecast = new WeatherForecast(combinedProperties);
                    hourlyWeatherForecastJson = objectMapper.writeValueAsString(hourlyWeatherForecast);
                }
            } else {
                log.info("No weather forecast json found so just setting it new.");
                this.hourlyWeatherForecast = hourlyWeatherForecast;
                this.hourlyWeatherForecastJson = objectMapper.writeValueAsString(hourlyWeatherForecast);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Agenda agenda)) return false;
        return Objects.equals(id, agenda.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
