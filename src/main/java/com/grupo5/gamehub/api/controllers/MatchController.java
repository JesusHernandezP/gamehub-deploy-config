package com.grupo5.gamehub.api.controllers;

import com.grupo5.gamehub.api.dtos.matches.MatchGenerationRequest;
import com.grupo5.gamehub.api.dtos.matches.MatchResponse;
import com.grupo5.gamehub.api.dtos.matches.MatchResultUpdateRequest;
import com.grupo5.gamehub.infraestructure.services.MatchService;
import com.grupo5.gamehub.infraestructure.services.TournamentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Emparejamientos y resultados", description = "Endpoints para la generación, consulta y actualización de partidas.")
public class MatchController {

  private final MatchService matchService;
  private final TournamentService tournamentService;

  @Autowired
  public MatchController(MatchService matchService, TournamentService tournamentService) {
    this.matchService = matchService;
    this.tournamentService = tournamentService;
  }

  private Long getAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = null;

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("Usuario no autenticado.");
    }

    if (authentication.getPrincipal() instanceof UserDetails) {
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      username = userDetails.getUsername();
    } else if (authentication.getPrincipal() instanceof String) {
      username = (String) authentication.getPrincipal();
    } else {
      throw new IllegalStateException("Formato de principal de autenticación desconocido.");
    }

    if (username == null) {
      throw new IllegalStateException("No se pudo obtener el nombre de usuario del contexto de seguridad.");
    }

    return tournamentService.getUserIdByUsername(username);
  }

  @Operation(summary = "Generar emparejamientos automáticos por ronda", description = "Permite a un ADMIN generar nuevas partidas para un torneo en una ronda específica.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Emparejamientos generados exitosamente",
                  content = @Content(schema = @Schema(implementation = MatchResponse.class))),
          @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. torneo no encontrado, ya hay emparejamientos para la ronda)",
                  content = @Content),
          @ApiResponse(responseCode = "401", description = "No autorizado (falta token JWT o no válido)",
                  content = @Content),
          @ApiResponse(responseCode = "403", description = "Acceso denegado (solo usuarios con rol ADMIN)",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/generate/{tournamentId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<MatchResponse>> generateMatches(
          @Parameter(description = "ID del torneo para el cual generar los emparejamientos")
          @PathVariable Long tournamentId,
          @RequestBody(description = "Detalles para la generación de emparejamientos, como el número de ronda", required = true,
                  content = @Content(schema = @Schema(implementation = MatchGenerationRequest.class)))
          @org.springframework.web.bind.annotation.RequestBody MatchGenerationRequest request) {
    try {
      List<MatchResponse> matches = matchService.generateMatches(tournamentId, request);
      return new ResponseEntity<>(matches, HttpStatus.CREATED);
    } catch (IllegalArgumentException | IllegalStateException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Consultar una partida", description = "Permite a cualquier usuario obtener los detalles de una partida específica.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Detalles de la partida devueltos exitosamente",
                  content = @Content(schema = @Schema(implementation = MatchResponse.class))),
          @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<MatchResponse> getMatchById(
          @Parameter(description = "ID de la partida a consultar")
          @PathVariable Long id) {
    try {
      MatchResponse match = matchService.getMatchById(id);
      return new ResponseEntity<>(match, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Actualizar el resultado de una partida", description = "Permite a un ADMIN o sistema interno actualizar el resultado de una partida. Una partida completada no puede ser modificada.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Resultado de la partida actualizado exitosamente",
                  content = @Content(schema = @Schema(implementation = MatchResponse.class))),
          @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. partida no encontrada, resultado inválido, partida ya completada)",
                  content = @Content),
          @ApiResponse(responseCode = "401", description = "No autorizado (falta token JWT o no válido)",
                  content = @Content),
          @ApiResponse(responseCode = "403", description = "Acceso denegado (solo usuarios con rol ADMIN)",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @SecurityRequirement(name = "bearerAuth")
  @PutMapping("/{id}/result")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MatchResponse> updateMatchResult(
          @Parameter(description = "ID de la partida a actualizar")
          @PathVariable Long id,
          @RequestBody(description = "Detalles del resultado de la partida", required = true,
                  content = @Content(schema = @Schema(implementation = MatchResultUpdateRequest.class)))
          @org.springframework.web.bind.annotation.RequestBody MatchResultUpdateRequest request) {
    try {
      MatchResponse updatedMatch = matchService.updateMatchResult(id, request);
      return new ResponseEntity<>(updatedMatch, HttpStatus.OK);
    } catch (IllegalArgumentException | IllegalStateException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Listar partidas por torneo", description = "Permite a cualquier usuario obtener todas las partidas asociadas a un torneo específico.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Lista de partidas del torneo devuelta exitosamente",
                  content = @Content(schema = @Schema(implementation = MatchResponse.class))),
          @ApiResponse(responseCode = "404", description = "Torneo no encontrado",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @GetMapping("/tournament/{tournamentId}")
  public ResponseEntity<List<MatchResponse>> getMatchesByTournament(
          @Parameter(description = "ID del torneo para obtener sus partidas")
          @PathVariable Long tournamentId) {
    try {
      List<MatchResponse> matches = matchService.getMatchesByTournament(tournamentId);
      return new ResponseEntity<>(matches, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}