package com.grupo5.gamehub.api.dtos.tournaments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentCreationRequest {
  @NotBlank(message = "El nombre del torneo no puede estar vacío")
  private String name;

  @NotNull(message = "La capacidad máxima de jugadores no puede ser nula")
  @Min(value = 2, message = "Un torneo debe tener al menos 2 jugadores")
  private Integer maxPlayers;
}