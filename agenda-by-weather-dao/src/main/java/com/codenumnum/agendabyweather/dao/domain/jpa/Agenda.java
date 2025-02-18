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
    public void updateGeneralWeatherForecast(WeatherForecast updatedGeneralWeatherForecast, ObjectMapper objectMapper) {
            if (StringUtils.hasText(this.generalWeatherForecastJson)) {
                var forecastFromJson = objectMapper.readValue(this.generalWeatherForecastJson, WeatherForecast.class);
                if(updatedGeneralWeatherForecast != null) {
                    var oldProperties = forecastFromJson.properties();
                    var newProperties = updatedGeneralWeatherForecast.properties();
                    if (oldProperties.weatherNeedsUpdate(newProperties)) {
                        var combinedProperties = oldProperties.updateWeather(newProperties, true);
                        this.generalWeatherForecast = new WeatherForecast(combinedProperties);
                        this.generalWeatherForecastJson = objectMapper.writeValueAsString(this.generalWeatherForecast);
                    }
                }

                if(this.generalWeatherForecast == null) {
                    // The first time the app loads the general forecast will be null so if this
                    // happens, like in a restart, load the json back into the object for the front
                    // end.
                    this.generalWeatherForecast = forecastFromJson;
                }
            } else {
                log.info("No general weather forecast json found so just setting it new.");
                if(updatedGeneralWeatherForecast != null) {
                    this.generalWeatherForecast = updatedGeneralWeatherForecast;
                    this.generalWeatherForecastJson = objectMapper.writeValueAsString(updatedGeneralWeatherForecast);
                }
            }
    }

    @SneakyThrows
    public void updateHourlyWeatherForecasts(WeatherForecast updatedHourlyWeatherForecast, ObjectMapper objectMapper) {
            if (StringUtils.hasText(this.hourlyWeatherForecastJson)) {
                var forecastFromJson = objectMapper.readValue(this.hourlyWeatherForecastJson, WeatherForecast.class);

                if(updatedHourlyWeatherForecast != null) {
                    var oldProperties = forecastFromJson.properties();
                    var newProperties = updatedHourlyWeatherForecast.properties();
                    if (oldProperties.weatherNeedsUpdate(newProperties)) {
                        var combinedProperties = oldProperties.updateWeather(newProperties, false);
                        this.hourlyWeatherForecast = new WeatherForecast(combinedProperties);
                        hourlyWeatherForecastJson = objectMapper.writeValueAsString(this.hourlyWeatherForecast);
                    }
                }

                if(this.hourlyWeatherForecast == null) {
                    // The first time the app loads the general forecast will be null so if this
                    // happens, like in a restart, load the json back into the object for the front
                    // end.
                    this.hourlyWeatherForecast = forecastFromJson;
                }
            } else {
                log.info("No hourly weather forecast json found so just setting it new.");
                if(updatedHourlyWeatherForecast != null) {
                    this.hourlyWeatherForecast = updatedHourlyWeatherForecast;
                    this.hourlyWeatherForecastJson = objectMapper.writeValueAsString(updatedHourlyWeatherForecast);
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
