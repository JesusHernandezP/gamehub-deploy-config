package com.grupo5.gamehub.api.controllers;

import com.grupo5.gamehub.api.dtos.AuthResponse;
import com.grupo5.gamehub.api.dtos.LoginRequest;
import com.grupo5.gamehub.api.dtos.RegisterRequest;
import com.grupo5.gamehub.infraestructure.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Gestión de usuarios", description = "Endpoints para registro y autenticación de usuarios.")
public class AuthController {

  private final AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "Registrar un nuevo usuario", description = "Crea un nuevo usuario con rol PLAYER y devuelve un token JWT.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente y JWT devuelto",
                  content = @Content(schema = @Schema(implementation = AuthResponse.class))),
          @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. usuario ya existe, datos incompletos)",
                  content = @Content)
  })
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
    AuthResponse response = authService.register(request);
    if (response.getToken() != null) {
      return new ResponseEntity<>(response, HttpStatus.CREATED);
    } else {
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
  }

  @Operation(summary = "Iniciar sesión de usuario", description = "Valida las credenciales de un usuario y devuelve un token JWT.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Credenciales válidas, JWT devuelto",
                  content = @Content(schema = @Schema(implementation = AuthResponse.class))),
          @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                  content = @Content)
  })
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    if (response.getToken() != null) {
      return new ResponseEntity<>(response, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
  }
}