package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.ranking.TournamentRankingResponse;

public interface RankingService {

  TournamentRankingResponse getTournamentRanking(Long tournamentId);
}