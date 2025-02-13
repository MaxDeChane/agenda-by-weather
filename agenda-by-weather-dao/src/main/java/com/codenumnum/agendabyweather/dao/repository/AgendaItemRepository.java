package com.codenumnum.agendabyweather.dao.repository;

import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import com.codenumnum.agendabyweather.dao.domain.jpa.AgendaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgendaItemRepository extends JpaRepository<AgendaItem, String> {

    Optional<AgendaItem> findByName(String name);

    /*
    These should be called one after the other to delete the entry in the joining table between
    agenda and items before deleting the agenda item. This is done separately since sqlite doesn't
    support them in one.
     */
    @Modifying
    @NativeQuery(value = "delete from AGENDA_AGENDA_ITEMS where AGENDA_ITEMS_ID = (select ID from AGENDA_ITEM where NAME = ?2) and AGENDA_ID = (select ID from AGENDA where LAT_LON = ?1);")
    void deleteAgendaItemsEntry(String latLon, String name);

    @Modifying
    @NativeQuery(value = "delete from AGENDA_ITEM where NAME = ?1;")
    void deleteAgendaItemByName(String name);
}
