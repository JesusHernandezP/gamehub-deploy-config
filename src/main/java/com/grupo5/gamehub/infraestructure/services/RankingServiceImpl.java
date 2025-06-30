package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.ranking.PlayerRankingDto;
import com.grupo5.gamehub.api.dtos.ranking.TournamentRankingResponse;
import com.grupo5.gamehub.domain.entities.Match;
import com.grupo5.gamehub.domain.entities.Tournament;
import com.grupo5.gamehub.domain.enums.MatchStatus;
import com.grupo5.gamehub.domain.repositories.MatchRepository;
import com.grupo5.gamehub.domain.repositories.TournamentRepository;
import com.grupo5.gamehub.domain.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

  private static final Logger log = LoggerFactory.getLogger(RankingServiceImpl.class);

  private final TournamentRepository tournamentRepository;
  private final MatchRepository matchRepository;
  private final UserRepository userRepository;

  @Autowired
  public RankingServiceImpl(TournamentRepository tournamentRepository,
                            MatchRepository matchRepository,
                            UserRepository userRepository) {
    this.tournamentRepository = tournamentRepository;
    this.matchRepository = matchRepository;
    this.userRepository = userRepository;
  }

  @Override
  public TournamentRankingResponse getTournamentRanking(Long tournamentId) {
    log.info("Iniciando cálculo de ranking para el torneo con ID: {}", tournamentId);

    Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado con ID: " + tournamentId));

    log.debug("Torneo encontrado: {}", tournament.getName());

    List<Match> completedMatches = matchRepository.findByTournamentIdAndStatus(tournamentId, MatchStatus.COMPLETED);
    log.debug("Número de partidas completadas encontradas: {}", completedMatches.size());

    Map<Long, PlayerStats> playerStatsMap = new HashMap<>();

    tournament.getPlayers().forEach(player -> {
      playerStatsMap.put(player.getId(), new PlayerStats(player.getId(), player.getUsername(),
              player.getRank(),
              0));
    });


    for (Match match : completedMatches) {
      log.trace("Procesando partida: ID {} - Player1: {} vs Player2: {} - Ganador: {}",
              match.getId(), match.getPlayer1().getUsername(), match.getPlayer2().getUsername(),
              match.getWinner() != null ? match.getWinner().getUsername() : "N/A");

      PlayerStats p1Stats = playerStatsMap.computeIfAbsent(match.getPlayer1().getId(),
              id -> new PlayerStats(match.getPlayer1().getId(), match.getPlayer1().getUsername(),
                      match.getPlayer1().getRank(), 0));
      p1Stats.gamesPlayed++;

      PlayerStats p2Stats = playerStatsMap.computeIfAbsent(match.getPlayer2().getId(),
              id -> new PlayerStats(match.getPlayer2().getId(), match.getPlayer2().getUsername(),
                      match.getPlayer2().getRank(), 0));
      p2Stats.gamesPlayed++;

      if (match.getWinner() != null) {
        if (match.getWinner().equals(match.getPlayer1())) {
          p1Stats.gamesWon++;
          p2Stats.gamesLost++;
          p1Stats.totalPoints += 3;
          log.trace("Player {} ganó, +3 puntos. Total P1: {}", p1Stats.username, p1Stats.totalPoints);
        } else if (match.getWinner().equals(match.getPlayer2())) {
          p2Stats.gamesWon++;
          p1Stats.gamesLost++;
          p2Stats.totalPoints += 3;
          log.trace("Player {} ganó, +3 puntos. Total P2: {}", p2Stats.username, p2Stats.totalPoints);
        }
      } else {
        log.trace("Partida sin ganador especificado (posible empate o incompleta si no hay resultado explicitado). No se asignan puntos.");
      }
    }

    List<PlayerRankingDto> rankingList = playerStatsMap.values().stream()
            .map(stats -> new PlayerRankingDto(
                    stats.userId,
                    stats.username,
                    stats.gamesPlayed,
                    stats.gamesWon,
                    stats.gamesLost,
                    stats.totalPoints,
                    stats.currentGlobalRank
            ))
            .sorted(Comparator.comparingInt(PlayerRankingDto::getTotalPoints).reversed()
                    .thenComparingInt(PlayerRankingDto::getGamesWon).reversed()
                    .thenComparing(PlayerRankingDto::getUsername))
            .collect(Collectors.toList());

    log.info("Ranking para torneo ID {} generado exitosamente con {} jugadores.", tournamentId, rankingList.size());
    return new TournamentRankingResponse(tournament.getId(), tournament.getName(), rankingList);
  }

  /**
   */
  private static class PlayerStats {
    Long userId;
    String username;
    int gamesPlayed = 0;
    int gamesWon = 0;
    int gamesLost = 0;
    int totalPoints = 0;
    Integer currentGlobalRank = null;

    public PlayerStats(Long userId, String username, Integer currentGlobalRank, Integer initialTournamentPoints) {
      this.userId = userId;
      this.username = username;
      this.currentGlobalRank = currentGlobalRank;
      this.totalPoints = initialTournamentPoints;
    }
  }
}