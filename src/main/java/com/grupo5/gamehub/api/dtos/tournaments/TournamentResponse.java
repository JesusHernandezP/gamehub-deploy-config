package com.grupo5.gamehub.api.dtos.tournaments;

import com.grupo5.gamehub.domain.enums.TournamentStatus;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.api.dtos.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResponse {
  private Long id;
  private String name;
  private TournamentStatus status;
  private Integer maxPlayers;
  private LocalDateTime createdAt;
  private UserResponse creator;
  private Set<UserResponse> players;

  public TournamentResponse(Long id, String name, TournamentStatus status,
                            Integer maxPlayers, LocalDateTime createdAt,
                            User creator, List<User> players) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.maxPlayers = maxPlayers;
    this.createdAt = createdAt;

    this.creator = new UserResponse(creator.getId(), creator.getUsername(), creator.getEmail());

    this.players = players.stream()
            .map(player -> new UserResponse(player.getId(), player.getUsername(), player.getEmail()))
            .collect(Collectors.toSet());
  }
}