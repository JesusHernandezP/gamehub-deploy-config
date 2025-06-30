package com.grupo5.gamehub.api.controllers;

import com.grupo5.gamehub.api.dtos.tournaments.TournamentCreationRequest;
import com.grupo5.gamehub.api.dtos.tournaments.TournamentJoinRequest;
import com.grupo5.gamehub.api.dtos.tournaments.TournamentResponse;
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
@RequestMapping("/api/tournaments")
@Tag(name = "Gestión de torneos", description = "Endpoints para la creación, unión y consulta de torneos.")
public class TournamentController {

  private final TournamentService tournamentService;

  @Autowired
  public TournamentController(TournamentService tournamentService) {
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

  @Operation(
          summary = "Crear un nuevo torneo",
          description = "Permite a un usuario con rol 'ADMIN' crear un nuevo torneo de videojuegos. Requiere autenticación con rol ADMIN.",
          security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Torneo creado exitosamente",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = TournamentResponse.class))),
          @ApiResponse(responseCode = "400", description = "Solicitud inválida: Datos del torneo incorrectos, incompletos o ya existe un torneo con el mismo nombre",
                  content = @Content),
          @ApiResponse(responseCode = "401", description = "No autorizado (falta token JWT o no válido)",
                  content = @Content),
          @ApiResponse(responseCode = "403", description = "Acceso denegado (el usuario no tiene el rol 'ADMIN')",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<TournamentResponse> createTournament(
          @RequestBody(description = "Datos para la creación del torneo", required = true,
                  content = @Content(schema = @Schema(implementation = TournamentCreationRequest.class)))
          @org.springframework.web.bind.annotation.RequestBody TournamentCreationRequest request) {
    Long creatorId;
    try {
      creatorId = getAuthenticatedUserId();
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    try {
      TournamentResponse newTournament = tournamentService.createTournament(request, creatorId);
      return new ResponseEntity<>(newTournament, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }


  @Operation(summary = "Listar todos los torneos", description = "Permite a cualquier usuario obtener una lista de todos los torneos disponibles. Acceso público.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Lista de torneos devuelta exitosamente",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = TournamentResponse.class))),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @GetMapping
  public ResponseEntity<List<TournamentResponse>> getAllTournaments() {
    List<TournamentResponse> tournaments = tournamentService.getAllTournaments();
    return new ResponseEntity<>(tournaments, HttpStatus.OK);
  }


  @Operation(summary = "Obtener detalles de un torneo por ID", description = "Permite a cualquier usuario obtener los detalles de un torneo específico utilizando su ID. Acceso público.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Detalles del torneo devueltos exitosamente",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = TournamentResponse.class))),
          @ApiResponse(responseCode = "404", description = "Torneo no encontrado",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<TournamentResponse> getTournamentById(
          @Parameter(description = "ID del torneo a consultar")
          @PathVariable Long id) {
    try {
      TournamentResponse tournament = tournamentService.getTournamentById(id);
      return new ResponseEntity<>(tournament, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }


  @Operation(
          summary = "Unir a un usuario (jugador) a un torneo",
          description = "Permite a un usuario autenticado con rol 'PLAYER' unirse a un torneo existente. Requiere autenticación.",
          security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Usuario unido al torneo exitosamente",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = TournamentResponse.class))),
          @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. torneo no encontrado, usuario ya unido, torneo lleno)",
                  content = @Content),
          @ApiResponse(responseCode = "401", description = "No autorizado (falta token JWT o no válido)",
                  content = @Content),
          @ApiResponse(responseCode = "403", description = "Acceso denegado (el usuario no tiene el rol 'PLAYER', si aplica esta restricción)",
                  content = @Content)
  })
  @PostMapping("/{tournamentId}/join")
  @PreAuthorize("isAuthenticated()") // O si usas roles: @PreAuthorize("hasRole('PLAYER')")
  public ResponseEntity<TournamentResponse> joinTournament(
          @Parameter(description = "ID del torneo al que el usuario desea unirse")
          @PathVariable Long tournamentId,
          @RequestBody(description = "Datos adicionales para unirse al torneo (opcional)", required = false,
                  content = @Content(schema = @Schema(implementation = TournamentJoinRequest.class)))
          @org.springframework.web.bind.annotation.RequestBody(required = false) TournamentJoinRequest request) {
    Long userId;
    try {
      userId = getAuthenticatedUserId();
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    try {
      TournamentResponse updatedTournament = tournamentService.joinTournament(tournamentId, userId);
      return new ResponseEntity<>(updatedTournament, HttpStatus.OK);
    } catch (IllegalArgumentException | IllegalStateException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }
}