package com.codenumnum.agendabyweather.dao.domain.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AgendaItem {

    @Id
    @Builder.Default
    UUID id = UUID.randomUUID();
    @Column(unique = true, nullable = false)
    String name;
    OffsetDateTime startDateTime;
    OffsetDateTime endDateTime;
    @Size(max = 250)
    String description;

    /**
     * This is to take an updated unmanaged object from the request and
     * transfer its fields to the hibernate managed object. As of now it
     * just does a one to one transfer so if more is needed in field
     * equality check before saving this is not the method to use.
     *
     * @param updatedAgendaItem
     */
    public void performFullAgendaTransfer(AgendaItem updatedAgendaItem) {
        this.name = updatedAgendaItem.getName();
        this.startDateTime = updatedAgendaItem.getStartDateTime();
        this.endDateTime = updatedAgendaItem.getEndDateTime();
        this.description = updatedAgendaItem.getDescription();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AgendaItem that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
