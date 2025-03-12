package com.codenumnum.agendabyweather.dao.domain.jpa;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
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
    String hourlyWeatherGeneratedAt;
    String hourlyWeatherUpdateTime;
    String generalWeatherForecastUrl;
    String generalWeatherGeneratedAt;
    String generalWeatherUpdateTime;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default
    List<AgendaItem> agendaItems = new ArrayList<>();
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<AgendaDay> agendaDays = new HashSet<>();

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
