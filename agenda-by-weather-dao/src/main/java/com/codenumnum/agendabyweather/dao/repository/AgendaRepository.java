package com.codenumnum.agendabyweather.dao.repository;

import com.codenumnum.agendabyweather.dao.domain.jpa.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgendaRepository extends JpaRepository<Agenda, String> {

    Optional<Agenda> findById(String id);
}
