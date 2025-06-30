package com.grupo5.gamehub.domain.repositories;

import com.grupo5.gamehub.domain.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByTournamentIdOrderBySentAtAsc(Long tournamentId);

  List<Message> findByMatchIdOrderBySentAtAsc(Long matchId);
}