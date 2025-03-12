package com.codenumnum.agendabyweather.dao.domain.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import java.util.Objects;

@Log4j2
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class AgendaDay {

    @EmbeddedId
    AgendaDayKey id;
    @Lob
    String hourlyWeatherForecastJson;
    @Lob
    String generalWeatherForecastJson;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AgendaDay agendaDay)) return false;
        return Objects.equals(id, agendaDay.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
