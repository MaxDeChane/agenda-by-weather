package com.codenumnum.agendabyweather.dao.repository;

import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, String> {

    Optional<Agenda> findByDefaultAgenda(boolean defaultAgenda);

    Optional<Agenda> findByLatLon(String latLon);
}
