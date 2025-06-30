package com.grupo5.gamehub.api.controllers;

import com.grupo5.gamehub.api.dtos.UserResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Gestión de usuarios", description = "Endpoints para la consulta de perfiles de usuario.")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Operation(
          summary = "Obtener perfil del usuario autenticado",
          description = "Permite a un usuario autenticado (PLAYER o ADMIN) obtener su propio perfil. Requiere un token JWT válido.",
          security = @SecurityRequirement(name = "bearerAuth")
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Perfil del usuario autenticado devuelto exitosamente",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = UserResponse.class))),
          @ApiResponse(responseCode = "401", description = "No autorizado (falta token JWT o no válido)",
                  content = @Content),
          @ApiResponse(responseCode = "404", description = "Perfil de usuario no encontrado (poco probable si está autenticado)",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getAuthenticatedUserProfile() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    String username;
    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetails) {
      username = ((UserDetails) principal).getUsername();
    } else {
      username = principal.toString();
    }

    Optional<UserResponse> userProfile = userService.getUserProfile(username);
    return userProfile.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @Operation(summary = "Obtener perfil de usuario por ID", description = "Permite a cualquier usuario consultar el perfil de otro usuario por su ID. Acceso público.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Perfil del usuario devuelto exitosamente",
                  content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = UserResponse.class))),
          @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                  content = @Content),
          @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                  content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserProfileById(
          @Parameter(description = "ID del usuario a consultar")
          @PathVariable Long id) {
    Optional<UserResponse> userProfile = userService.getUserProfileById(id);
    return userProfile.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}