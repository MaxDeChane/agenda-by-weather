package com.codenumnum.agendabyweather.dao.domain.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AgendaItem {

    @Id
    String name;
    OffsetDateTime startDateTime;
    OffsetDateTime endDateTime;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AgendaItem that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
