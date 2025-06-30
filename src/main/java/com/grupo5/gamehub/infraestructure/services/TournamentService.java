package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.tournaments.TournamentCreationRequest;
import com.grupo5.gamehub.api.dtos.tournaments.TournamentResponse;
import com.grupo5.gamehub.domain.entities.Tournament;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.enums.TournamentStatus;
import com.grupo5.gamehub.domain.repositories.TournamentRepository;
import com.grupo5.gamehub.domain.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentService {

  private final TournamentRepository tournamentRepository;
  private final UserRepository userRepository;

  @Autowired
  public TournamentService(TournamentRepository tournamentRepository, UserRepository userRepository) {
    this.tournamentRepository = tournamentRepository;
    this.userRepository = userRepository;
  }

  public Long getUserIdByUsername(String username) {
    return userRepository.findByUsername(username)
            .map(User::getId)
            .orElseThrow(() -> new RuntimeException("Usuario '" + username + "' no encontrado para obtener su ID."));
  }


  public TournamentResponse createTournament(TournamentCreationRequest request, Long creatorId) {
    if (tournamentRepository.findByName(request.getName()).isPresent()) {
      throw new IllegalArgumentException("Ya existe un torneo con este nombre.");
    }

    User creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new IllegalArgumentException("Creador de torneo no encontrado."));

    Tournament tournament = new Tournament();
    tournament.setName(request.getName());
    tournament.setMaxPlayers(request.getMaxPlayers());
    tournament.setCreator(creator);
    tournament.setCreatedAt(LocalDateTime.now());
    tournament.setStatus(TournamentStatus.CREATED);

    Tournament savedTournament = tournamentRepository.save(tournament);
    return convertToDto(savedTournament);
  }

  public List<TournamentResponse> getAllTournaments() {
    return tournamentRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
  }

  public TournamentResponse getTournamentById(Long id) {
    return tournamentRepository.findById(id)
            .map(this::convertToDto)
            .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado."));
  }

  public TournamentResponse joinTournament(Long tournamentId, Long userId) {
    Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado."));

    User player = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado."));

    if (tournament.getStatus() != TournamentStatus.CREATED) {
      throw new IllegalStateException("Solo puedes unirte a torneos en estado 'CREATED'.");
    }
    if (tournament.getPlayers().size() >= tournament.getMaxPlayers()) {
      throw new IllegalStateException("El torneo ya está lleno.");
    }
    if (tournament.getPlayers().contains(player)) {
      throw new IllegalArgumentException("El jugador ya está registrado en este torneo.");
    }

    tournament.getPlayers().add(player);
    Tournament updatedTournament = tournamentRepository.save(tournament);
    return convertToDto(updatedTournament);
  }

  private TournamentResponse convertToDto(Tournament tournament) {
    return new TournamentResponse(
            tournament.getId(),
            tournament.getName(),
            tournament.getStatus(),
            tournament.getMaxPlayers(),
            tournament.getCreatedAt(),
            tournament.getCreator(),
            tournament.getPlayers()
    );
  }


}