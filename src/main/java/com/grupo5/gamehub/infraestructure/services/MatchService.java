package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.matches.MatchGenerationRequest;
import com.grupo5.gamehub.api.dtos.matches.MatchResponse;
import com.grupo5.gamehub.api.dtos.matches.MatchResultUpdateRequest;

import java.util.List;

public interface MatchService {
  List<MatchResponse> generateMatches(Long tournamentId, MatchGenerationRequest request);
  MatchResponse getMatchById(Long id);
  MatchResponse updateMatchResult(Long id, MatchResultUpdateRequest request);
  List<MatchResponse> getMatchesByTournament(Long tournamentId);

}