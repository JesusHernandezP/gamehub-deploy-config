package com.grupo5.gamehub.api.dtos;

import com.grupo5.gamehub.domain.enums.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
  private Long id;
  private String username;
  private String email;
  private Role role;
  private Integer points;
  private Integer rank;

  public UserResponse(Long id, String username, String email) {
    this.id = id;
    this.username = username;
    this.email = email;


  }
}