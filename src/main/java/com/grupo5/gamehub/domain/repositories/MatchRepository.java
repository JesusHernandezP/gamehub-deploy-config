package com.grupo5.gamehub.domain.repositories;

import com.grupo5.gamehub.domain.entities.Match;
import com.grupo5.gamehub.domain.entities.Tournament;
import com.grupo5.gamehub.domain.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
  // Método para encontrar partidas por torneo y número de ronda
  List<Match> findByTournamentAndRoundNumber(Tournament tournament, Integer roundNumber);

  // Método para encontrar todas las partidas de un torneo
  List<Match> findByTournament(Tournament tournament);

  // Nuevo método para encontrar partidas por torneo y estado
  List<Match> findByTournamentIdAndStatus(Long tournamentId, MatchStatus status);
}