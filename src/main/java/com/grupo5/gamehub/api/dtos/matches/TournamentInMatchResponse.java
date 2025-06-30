package com.grupo5.gamehub.api.dtos.matches;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TournamentInMatchResponse {
  private Long id;
  private String name;
}