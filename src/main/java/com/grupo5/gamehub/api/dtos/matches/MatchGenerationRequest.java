package com.grupo5.gamehub.api.dtos.matches;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchGenerationRequest {
  @NotNull(message = "El número de ronda no puede ser nulo.")
  @Min(value = 1, message = "El número de ronda debe ser al menos 1.")
  private Integer roundNumber;
}