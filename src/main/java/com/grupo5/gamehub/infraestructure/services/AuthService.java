package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.LoginRequest;
import com.grupo5.gamehub.api.dtos.RegisterRequest;
import com.grupo5.gamehub.api.dtos.AuthResponse;

public interface AuthService {
  AuthResponse register(RegisterRequest request);
  AuthResponse login(LoginRequest request);
}