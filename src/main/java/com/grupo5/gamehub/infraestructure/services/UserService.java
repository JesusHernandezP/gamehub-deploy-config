package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.UserResponse;

import java.util.Optional;

public interface UserService {
  Optional<UserResponse> getUserProfile(String username);
  Optional<UserResponse> getUserProfileById(Long id);
  Long getUserIdByUsername(String username);
}