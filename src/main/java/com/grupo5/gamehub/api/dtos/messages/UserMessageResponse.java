package com.grupo5.gamehub.api.dtos.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageResponse {
  private Long id;
  private String username;
}