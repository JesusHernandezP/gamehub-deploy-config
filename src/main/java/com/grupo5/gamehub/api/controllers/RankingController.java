package com.grupo5.gamehub.api.controllers;

import com.grupo5.gamehub.api.dtos.ranking.TournamentRankingResponse;
import com.grupo5.gamehub.infraestructure.services.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Ranking", description = "Endpoints para consultar la tabla de clasificación de torneos.")
public class RankingController {

  private final RankingService rankingService;

  @Autowired
  public RankingController(RankingService rankingService) {
    this.rankingService = rankingService;
  }

  @Operation(summary = "Obtener tabla de clasificación de un torneo", description = "Permite a cualquier usuario consultar la tabla de clasificación de un torneo específico.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Tabla de clasificación del torneo devuelta exitosamente",
                  content = @Content(schema = @Schema(implementation = TournamentRankingResponse.class))),
          @ApiResponse(responseCode = "404", description = "Torneo no encontrado o no hay ranking disponible aún",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @GetMapping("/tournaments/{tournamentId}/ranking")
  public ResponseEntity<TournamentRankingResponse> getTournamentRanking(
          @Parameter(description = "ID del torneo para obtener su ranking")
          @PathVariable Long tournamentId) {
    try {
      TournamentRankingResponse ranking = rankingService.getTournamentRanking(tournamentId);
      return ResponseEntity.ok(ranking);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}