package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.matches.MatchGenerationRequest;
import com.grupo5.gamehub.api.dtos.matches.MatchResponse;
import com.grupo5.gamehub.api.dtos.matches.MatchResultUpdateRequest;
import com.grupo5.gamehub.api.dtos.matches.UserInMatchResponse;
import com.grupo5.gamehub.api.dtos.matches.TournamentInMatchResponse;
import com.grupo5.gamehub.domain.entities.Match;
import com.grupo5.gamehub.domain.entities.Tournament;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.enums.MatchStatus;
import com.grupo5.gamehub.domain.enums.Result;
import com.grupo5.gamehub.domain.enums.TournamentStatus;
import com.grupo5.gamehub.domain.repositories.MatchRepository;
import com.grupo5.gamehub.domain.repositories.TournamentRepository;
import com.grupo5.gamehub.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

  private final MatchRepository matchRepository;
  private final TournamentRepository tournamentRepository;
  private final UserRepository userRepository;

  public MatchServiceImpl(MatchRepository matchRepository, TournamentRepository tournamentRepository, UserRepository userRepository) {
    this.matchRepository = matchRepository;
    this.tournamentRepository = tournamentRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public List<MatchResponse> generateMatches(Long tournamentId, MatchGenerationRequest request) {
    Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado con ID: " + tournamentId));

    if (tournament.getPlayers().size() < 2) {
      throw new IllegalStateException("El torneo necesita al menos 2 jugadores para generar partidas.");
    }
    if (request.getRoundNumber() == null || request.getRoundNumber() <= 0) {
      throw new IllegalArgumentException("El número de ronda debe ser un valor positivo.");
    }

    if (tournament.getStatus() == TournamentStatus.COMPLETED || tournament.getStatus() == TournamentStatus.CANCELLED) {
      throw new IllegalStateException("No se pueden generar partidas para un torneo finalizado o cancelado.");
    }

    List<Match> existingMatchesForRound = matchRepository.findByTournamentAndRoundNumber(tournament, request.getRoundNumber());
    if (!existingMatchesForRound.isEmpty()) {
      throw new IllegalStateException("Ya existen partidas generadas para la ronda " + request.getRoundNumber() + " de este torneo.");
    }


    List<User> players = new ArrayList<>(tournament.getPlayers());
    Collections.shuffle(players);

    List<Match> generatedMatches = new ArrayList<>();
    for (int i = 0; i + 1 < players.size(); i += 2) {
      User player1 = players.get(i);
      User player2 = players.get(i + 1);

      Match match = new Match(tournament, player1, player2, request.getRoundNumber());
      generatedMatches.add(match);
    }

    if (players.size() % 2 != 0) {
      System.out.println("Jugador " + players.get(players.size() - 1).getUsername() + " tiene bye en la ronda " + request.getRoundNumber());
    }

    List<Match> savedMatches = matchRepository.saveAll(generatedMatches);

    if (tournament.getStatus() == TournamentStatus.CREATED) {
      tournament.setStatus(TournamentStatus.IN_PROGRESS);
      tournamentRepository.save(tournament);
    }


    return savedMatches.stream()
            .map(this::convertToMatchResponse)
            .collect(Collectors.toList());
  }

  @Override
  public MatchResponse getMatchById(Long id) {
    return matchRepository.findById(id)
            .map(this::convertToMatchResponse)
            .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada con ID: " + id));
  }

  @Override
  @Transactional
  public MatchResponse updateMatchResult(Long id, MatchResultUpdateRequest request) {
    Match match = matchRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada con ID: " + id));

    if (match.getStatus() == MatchStatus.COMPLETED) {
      throw new IllegalStateException("Esta partida ya ha sido completada y su resultado no puede ser modificado.");
    }

    if (request.getResult() == Result.PENDING) {
      throw new IllegalArgumentException("El resultado de la partida no puede ser 'PENDING' al actualizar.");
    }


    // Asignar el ganador basado en el resultado
    User winner = null;
    if (request.getWinnerId() != null) {
      winner = userRepository.findById(request.getWinnerId())
              .orElseThrow(() -> new IllegalArgumentException("Ganador no encontrado con ID: " + request.getWinnerId()));

      if (!match.getPlayer1().getId().equals(winner.getId()) && !match.getPlayer2().getId().equals(winner.getId())) {
        throw new IllegalArgumentException("El ganador proporcionado no es un jugador válido para esta partida.");
      }
      if (request.getResult() == Result.DRAW) {
        throw new IllegalArgumentException("No se puede especificar un ganador si el resultado es EMPATE.");
      }
    } else {
      if (request.getResult() != Result.DRAW) {
        throw new IllegalArgumentException("Si el resultado no es EMPATE, se debe especificar un ganador (winnerId).");
      }
    }
    match.setWinner(winner);
    match.setResult(request.getResult());
    match.setStatus(MatchStatus.COMPLETED);

    Match updatedMatch = matchRepository.save(match);


    return convertToMatchResponse(updatedMatch);
  }

  @Override
  public List<MatchResponse> getMatchesByTournament(Long tournamentId) {
    Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado con ID: " + tournamentId));

    List<Match> matches = matchRepository.findByTournament(tournament);
    return matches.stream()
            .map(this::convertToMatchResponse)
            .collect(Collectors.toList());
  }

  private MatchResponse convertToMatchResponse(Match match) {
    UserInMatchResponse player1Dto = null;
    if (match.getPlayer1() != null) {
      player1Dto = new UserInMatchResponse(match.getPlayer1().getId(), match.getPlayer1().getUsername(), match.getPlayer1().getEmail());
    }

    UserInMatchResponse player2Dto = null;
    if (match.getPlayer2() != null) {
      player2Dto = new UserInMatchResponse(match.getPlayer2().getId(), match.getPlayer2().getUsername(), match.getPlayer2().getEmail());
    }

    UserInMatchResponse winnerDto = null;
    if (match.getWinner() != null) {
      winnerDto = new UserInMatchResponse(match.getWinner().getId(), match.getWinner().getUsername(), match.getWinner().getEmail());
    }

    TournamentInMatchResponse tournamentDto = null;
    if (match.getTournament() != null) {
      tournamentDto = new TournamentInMatchResponse(match.getTournament().getId(), match.getTournament().getName());
    }

    return new MatchResponse(
            match.getId(),
            tournamentDto,
            player1Dto,
            player2Dto,
            winnerDto,
            match.getResult(),
            match.getStatus(),
            match.getRoundNumber()
    );
  }
}