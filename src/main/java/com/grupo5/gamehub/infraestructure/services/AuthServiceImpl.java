package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.AuthResponse;
import com.grupo5.gamehub.api.dtos.LoginRequest;
import com.grupo5.gamehub.api.dtos.RegisterRequest;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.enums.Role;
import com.grupo5.gamehub.domain.repositories.UserRepository;
import com.grupo5.gamehub.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;

  @Autowired
  public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                         JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.authenticationManager = authenticationManager;
  }

  @Override
  public AuthResponse register(RegisterRequest request) {
    // Validaciones básicas (puedes añadir más)
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      return new AuthResponse(null, "Username already exists");
    }
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      return new AuthResponse(null, "Email already exists");
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(Role.PLAYER);
    userRepository.save(user);


    String token = jwtUtil.generateToken(user.getUsername());
    return new AuthResponse(token, "User registered successfully");
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    try {
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
      );

      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String token = jwtUtil.generateToken(userDetails.getUsername());
      return new AuthResponse(token, "Login successful");
    } catch (Exception e) {
      return new AuthResponse(null, "Invalid credentials");
    }
  }
}