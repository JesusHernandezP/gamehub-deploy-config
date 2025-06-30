package com.grupo5.gamehub.api.controllers;

import com.grupo5.gamehub.api.dtos.messages.MessageRequest;
import com.grupo5.gamehub.api.dtos.messages.MessageResponse;
import com.grupo5.gamehub.infraestructure.services.MessageService;
import com.grupo5.gamehub.infraestructure.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Chat básico (HTTP polling)", description = "Endpoints para el envío y recepción de mensajes de chat en torneos y partidas.")
public class MessageController {

  private final MessageService messageService;
  private final UserService userService;

  @Autowired
  public MessageController(MessageService messageService, UserService userService) {
    this.messageService = messageService;
    this.userService = userService;
  }

  @Operation(summary = "Listar mensajes del torneo", description = "Permite a usuarios autenticados (PLAYER, ADMIN) listar los mensajes de un torneo específico.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Lista de mensajes del torneo devuelta exitosamente",
                  content = @Content(schema = @Schema(implementation = MessageResponse.class))),
          @ApiResponse(responseCode = "401", description = "No autorizado (falta token JWT o no válido)",
                  content = @Content),
          @ApiResponse(responseCode = "403", description = "Acceso denegado (el usuario no es participante ni ADMIN del torneo)",
                  content = @Content),
          @ApiResponse(responseCode = "404", description = "Torneo no encontrado",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/tournaments/{tournamentId}/messages")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<MessageResponse>> getTournamentMessages(
          @Parameter(description = "ID del torneo para obtener los mensajes")
          @PathVariable Long tournamentId) {
    try {
      List<MessageResponse> messages = messageService.getTournamentMessages(tournamentId);
      return ResponseEntity.ok(messages);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @Operation(summary = "Enviar mensaje al torneo", description = "Permite a usuarios autenticados (PLAYER, ADMIN) enviar un mensaje a un torneo específico.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Mensaje enviado exitosamente",
                  content = @Content(schema = @Schema(implementation = MessageResponse.class))),
          @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. cuerpo del mensaje vacío)",
                  content = @Content),
          @ApiResponse(responseCode = "401", description = "No autorizado (falta token JWT o no válido)",
                  content = @Content),
          @ApiResponse(responseCode = "403", description = "Acceso denegado (el usuario no es participante ni ADMIN del torneo)",
                  content = @Content),
          @ApiResponse(responseCode = "404", description = "Torneo o remitente no encontrado",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/tournaments/{tournamentId}/messages")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<MessageResponse> sendMessageToTournament(
          @Parameter(description = "ID del torneo al que enviar el mensaje")
          @PathVariable Long tournamentId,
          @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Contenido del mensaje a enviar", required = true,
                  content = @Content(schema = @Schema(implementation = MessageRequest.class)))
          @Valid @RequestBody MessageRequest messageRequest) {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = null;
      if (authentication != null && authentication.isAuthenticated()) {
        if (authentication.getPrincipal() instanceof UserDetails) {
          username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
          username = (String) authentication.getPrincipal();
        }
      }

      if (username == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
      }

      Long senderId = userService.getUserIdByUsername(username);

      MessageResponse message = messageService.sendMessageToTournament(tournamentId, senderId, messageRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(message);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @Operation(summary = "Listar mensajes de la partida", description = "Permite a usuarios autenticados (PLAYER, ADMIN) listar los mensajes de una partida específica.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Lista de mensajes de la partida devuelta exitosamente",
                  content = @Content(schema = @Schema(implementation = MessageResponse.class))),
          @ApiResponse(responseCode = "401", description = "No autorizado (falta token JWT o no válido)",
                  content = @Content),
          @ApiResponse(responseCode = "403", description = "Acceso denegado (el usuario no es participante ni ADMIN de la partida)",
                  content = @Content),
          @ApiResponse(responseCode = "404", description = "Partida no encontrada",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/matches/{matchId}/messages")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<MessageResponse>> getMatchMessages(
          @Parameter(description = "ID de la partida para obtener los mensajes")
          @PathVariable Long matchId) {
    try {
      List<MessageResponse> messages = messageService.getMatchMessages(matchId);
      return ResponseEntity.ok(messages);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @Operation(summary = "Enviar mensaje a la partida", description = "Permite a usuarios autenticados (PLAYER, ADMIN) enviar un mensaje a una partida específica.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Mensaje enviado exitosamente",
                  content = @Content(schema = @Schema(implementation = MessageResponse.class))),
          @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. cuerpo del mensaje vacío)",
                  content = @Content),
          @ApiResponse(responseCode = "401", description = "No autorizado (falta token JWT o no válido)",
                  content = @Content),
          @ApiResponse(responseCode = "403", description = "Acceso denegado (el usuario no es participante ni ADMIN de la partida)",
                  content = @Content),
          @ApiResponse(responseCode = "404", description = "Partida o remitente no encontrado",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/matches/{matchId}/messages")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<MessageResponse> sendMessageToMatch(
          @Parameter(description = "ID de la partida a la que enviar el mensaje")
          @PathVariable Long matchId,
          @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Contenido del mensaje a enviar", required = true,
                  content = @Content(schema = @Schema(implementation = MessageRequest.class)))
          @Valid @RequestBody MessageRequest messageRequest) {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String username = null;
      if (authentication != null && authentication.isAuthenticated()) {
        if (authentication.getPrincipal() instanceof UserDetails) {
          username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
          username = (String) authentication.getPrincipal();
        }
      }

      if (username == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
      }

      Long senderId = userService.getUserIdByUsername(username);

      MessageResponse message = messageService.sendMessageToMatch(matchId, senderId, messageRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(message);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }
}