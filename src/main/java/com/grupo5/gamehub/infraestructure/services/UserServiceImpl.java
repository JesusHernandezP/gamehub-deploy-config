package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.UserResponse;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;



@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Autowired
  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<UserResponse> getUserProfile(String username) {
    return userRepository.findByUsername(username)
            .map(this::convertToUserResponse);
  }

  @Override
  public Optional<UserResponse> getUserProfileById(Long id) {
    return userRepository.findById(id)
            .map(this::convertToUserResponse);
  }

  @Override
  public Long getUserIdByUsername(String username) {
    return userRepository.findByUsername(username)
            .map(User::getId)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username: " + username));
  }


  private UserResponse convertToUserResponse(User user) {
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setRole(user.getRole());
    response.setPoints(user.getPoints());
    response.setRank(user.getRank());
    return response;
  }
}