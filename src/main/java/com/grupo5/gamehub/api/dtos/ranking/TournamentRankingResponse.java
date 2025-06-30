package com.grupo5.gamehub.api.dtos.ranking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRankingResponse {
  private Long tournamentId;
  private String tournamentName;
  private List<PlayerRankingDto> ranking;
}