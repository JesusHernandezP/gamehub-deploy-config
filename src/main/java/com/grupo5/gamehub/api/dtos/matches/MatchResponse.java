package com.grupo5.gamehub.api.dtos.matches;

import com.grupo5.gamehub.domain.enums.MatchStatus;
import com.grupo5.gamehub.domain.enums.Result;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchResponse {
  private Long id;
  private TournamentInMatchResponse tournament;
  private UserInMatchResponse player1;
  private UserInMatchResponse player2;
  private UserInMatchResponse winner;
  private Result result;
  private MatchStatus status;
  private Integer roundNumber;
}