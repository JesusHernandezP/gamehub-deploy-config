package com.grupo5.gamehub.api.dtos.matches;

import com.grupo5.gamehub.domain.enums.Result;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchResultUpdateRequest {
  private Long winnerId;

  @JsonProperty("result")
  @NotNull(message = "El resultado no puede ser nulo.")
  private Result result;


  @AssertTrue(message = "Si hay un ganador, el resultado no puede ser EMPATE. Si el resultado es EMPATE, no debe haber ganador.")
  private boolean isValidWinnerAndResultCombination() {
    if (winnerId != null && result == Result.DRAW) {
      return false;
    }
    if (winnerId == null && (result == Result.PLAYER1_WINS || result == Result.PLAYER2_WINS)) {
      return false;
    }
    return true;
  }
}