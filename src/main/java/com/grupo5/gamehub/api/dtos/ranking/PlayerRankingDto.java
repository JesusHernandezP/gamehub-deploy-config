package com.grupo5.gamehub.api.dtos.ranking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRankingDto {
  private Long userId;
  private String username;
  private int gamesPlayed;
  private int gamesWon;
  private int gamesLost;
  private int totalPoints;
  private Integer currentGlobalRank;
}