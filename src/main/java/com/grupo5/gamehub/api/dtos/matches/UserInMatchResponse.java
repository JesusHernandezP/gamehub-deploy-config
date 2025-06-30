package com.grupo5.gamehub.api.dtos.matches;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInMatchResponse {
  private Long id;
  private String username;
  private String email;
}