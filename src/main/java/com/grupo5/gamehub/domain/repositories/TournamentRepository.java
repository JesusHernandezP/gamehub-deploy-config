package com.grupo5.gamehub.domain.repositories;

import com.grupo5.gamehub.domain.entities.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

  Optional<Tournament> findByName(String name);


}