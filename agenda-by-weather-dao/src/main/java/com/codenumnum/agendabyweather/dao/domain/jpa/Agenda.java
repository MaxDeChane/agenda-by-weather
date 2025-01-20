package com.codenumnum.agendabyweather.dao.domain.jpa;

import com.codenumnum.agendabyweather.dao.domain.WeatherForecast;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Agenda {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    String latLon;
    @Column(nullable = false)
    boolean defaultAgenda;
     /*Don't save the actual weather forecast since it will be stored
     as just a json string in the db. If we want to filter based on any
     fields in the forecast they will be extracted out to their own columns
     but most filtering besides dates and locations will be done in code and
     not in the db.*/
    @Transient
    WeatherForecast weatherForecast;

    public Agenda(String latLon, boolean defaultAgenda, WeatherForecast weatherForecast) {
        this.latLon = latLon;
        this.defaultAgenda = defaultAgenda;
        this.weatherForecast = weatherForecast;
    }
}
