package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.UserResponse;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.enums.Role;
import com.grupo5.gamehub.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserServiceImpl userService;

  private User testUser;
  private UserResponse testUserResponse;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword("encodedPassword"); // Contraseña encriptada, no relevante para estos tests
    testUser.setRole(Role.PLAYER);
    testUser.setPoints(100);
    testUser.setRank(1);

    testUserResponse = new UserResponse();
    testUserResponse.setId(1L);
    testUserResponse.setUsername("testuser");
    testUserResponse.setEmail("test@example.com");
    testUserResponse.setRole(Role.PLAYER);
    testUserResponse.setPoints(100);
    testUserResponse.setRank(1);
  }

  @Test
  @DisplayName("Debe retornar el perfil de usuario por username cuando existe")
  void getUserProfile_shouldReturnUserProfile_whenUserExists() {
    when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

    Optional<UserResponse> result = userService.getUserProfile(testUser.getUsername());

    assertTrue(result.isPresent());
    assertEquals(testUserResponse.getId(), result.get().getId());
    assertEquals(testUserResponse.getUsername(), result.get().getUsername());
    assertEquals(testUserResponse.getEmail(), result.get().getEmail());
    assertEquals(testUserResponse.getRole(), result.get().getRole());
    assertEquals(testUserResponse.getPoints(), result.get().getPoints());
    assertEquals(testUserResponse.getRank(), result.get().getRank());
    verify(userRepository, times(1)).findByUsername(testUser.getUsername());
  }

  @Test
  @DisplayName("Debe retornar Optional.empty() al buscar perfil por username que no existe")
  void getUserProfile_shouldReturnEmpty_whenUserDoesNotExist() {
    String nonExistentUsername = "nonexistent";
    when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

    Optional<UserResponse> result = userService.getUserProfile(nonExistentUsername);

    assertFalse(result.isPresent());
    verify(userRepository, times(1)).findByUsername(nonExistentUsername);
  }

  @Test
  @DisplayName("Debe retornar el perfil de usuario por ID cuando existe")
  void getUserProfileById_shouldReturnUserProfile_whenUserExists() {
    when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

    Optional<UserResponse> result = userService.getUserProfileById(testUser.getId());

    assertTrue(result.isPresent());
    assertEquals(testUserResponse.getId(), result.get().getId());
    assertEquals(testUserResponse.getUsername(), result.get().getUsername());
    verify(userRepository, times(1)).findById(testUser.getId());
  }

  @Test
  @DisplayName("Debe retornar Optional.empty() al buscar perfil por ID que no existe")
  void getUserProfileById_shouldReturnEmpty_whenUserDoesNotExist() {
    Long nonExistentId = 99L;
    when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    Optional<UserResponse> result = userService.getUserProfileById(nonExistentId);

    assertFalse(result.isPresent());
    verify(userRepository, times(1)).findById(nonExistentId);
  }

  @Test
  @DisplayName("Debe retornar el ID del usuario por username cuando existe")
  void getUserIdByUsername_shouldReturnUserId_whenUserExists() {
    when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

    Long userId = userService.getUserIdByUsername(testUser.getUsername());

    assertEquals(testUser.getId(), userId);
    verify(userRepository, times(1)).findByUsername(testUser.getUsername());
  }

  @Test
  @DisplayName("Debe lanzar UsernameNotFoundException al buscar ID por username que no existe")
  void getUserIdByUsername_shouldThrowException_whenUserDoesNotExist() {
    String nonExistentUsername = "nonexistent";
    when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> userService.getUserIdByUsername(nonExistentUsername));
    verify(userRepository, times(1)).findByUsername(nonExistentUsername);
  }
}