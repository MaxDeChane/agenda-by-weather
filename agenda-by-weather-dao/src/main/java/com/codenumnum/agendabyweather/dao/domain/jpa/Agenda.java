package com.codenumnum.agendabyweather.dao.domain.jpa;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import java.time.OffsetDateTime;
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
    OffsetDateTime hourlyWeatherGeneratedAt;
    OffsetDateTime hourlyWeatherUpdateTime;
    String generalWeatherForecastUrl;
    OffsetDateTime generalWeatherGeneratedAt;
    OffsetDateTime generalWeatherUpdateTime;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default
    List<AgendaItem> agendaItems = new ArrayList<>();

    @JoinColumn(name = "agendaId")
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
