package com.codenumnum.agendabyweather.dao.domain.jpa;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
    @Transient
    WeatherForecast generalWeatherForecast;

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
