package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.UserResponse;
import com.grupo5.gamehub.api.dtos.tournaments.TournamentCreationRequest;
import com.grupo5.gamehub.api.dtos.tournaments.TournamentResponse;
import com.grupo5.gamehub.domain.entities.Tournament;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.enums.Role;
import com.grupo5.gamehub.domain.enums.TournamentStatus;
import com.grupo5.gamehub.domain.repositories.TournamentRepository;
import com.grupo5.gamehub.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

  @Mock
  private TournamentRepository tournamentRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private TournamentService tournamentService; // No es necesario 'Impl' si la clase se llama TournamentService

  private User adminUser;
  private User player1;
  private User player2;
  private Tournament testTournament;
  private TournamentCreationRequest creationRequest;
  private TournamentResponse expectedTournamentResponse;

  @BeforeEach
  void setUp() {
    adminUser = new User();
    adminUser.setId(1L);
    adminUser.setUsername("adminUser");
    adminUser.setEmail("admin@example.com");
    adminUser.setRole(Role.ADMIN);
    adminUser.setPassword("pass");

    player1 = new User();
    player1.setId(2L);
    player1.setUsername("player1");
    player1.setEmail("player1@example.com");
    player1.setRole(Role.PLAYER);
    player1.setPassword("pass");

    player2 = new User();
    player2.setId(3L);
    player2.setUsername("player2");
    player2.setEmail("player2@example.com");
    player2.setRole(Role.PLAYER);
    player2.setPassword("pass");

    testTournament = new Tournament();
    testTournament.setId(10L);
    testTournament.setName("Test Tournament");
    testTournament.setMaxPlayers(4);
    testTournament.setCreator(adminUser);
    testTournament.setCreatedAt(LocalDateTime.now());
    testTournament.setStatus(TournamentStatus.CREATED);
    testTournament.setPlayers(new ArrayList<>()); // Inicialmente sin jugadores

    creationRequest = new TournamentCreationRequest("New Tournament", 8);

    // Construir el DTO esperado para las verificaciones
    UserResponse adminUserResponse = new UserResponse(
            adminUser.getId(),
            adminUser.getUsername(),
            adminUser.getEmail(),
            adminUser.getRole(),
            adminUser.getPoints(),
            adminUser.getRank());

    expectedTournamentResponse = new TournamentResponse(
            testTournament.getId(),
            testTournament.getName(),
            testTournament.getStatus(),
            testTournament.getMaxPlayers(),
            testTournament.getCreatedAt(),
            adminUserResponse, // Se espera un UserResponse aquí
            testTournament.getPlayers().stream()
                    .map(p -> new UserResponse(p.getId(),
                            p.getUsername(),
                            p.getEmail(),
                            p.getRole(),
                            p.getPoints(),
                            p.getRank()))
                    .collect(Collectors.toSet())
    );
  }

  // --- getUserIdByUsername Tests ---
  @Test
  @DisplayName("Debe retornar el ID del usuario por username cuando existe")
  void getUserIdByUsername_shouldReturnUserId_whenUserExists() {
    when(userRepository.findByUsername(adminUser.getUsername())).thenReturn(Optional.of(adminUser));

    Long userId = tournamentService.getUserIdByUsername(adminUser.getUsername());

    assertEquals(adminUser.getId(), userId);
    verify(userRepository, times(1)).findByUsername(adminUser.getUsername());
  }

  @Test
  @DisplayName("Debe lanzar RuntimeException al buscar ID por username que no existe")
  void getUserIdByUsername_shouldThrowRuntimeException_whenUserDoesNotExist() {
    String nonExistentUsername = "nonexistent";
    when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> tournamentService.getUserIdByUsername(nonExistentUsername));
    verify(userRepository, times(1)).findByUsername(nonExistentUsername);
  }

  // --- createTournament Tests ---
  @Test
  @DisplayName("Debe crear un torneo exitosamente")
  void createTournament_shouldCreateTournamentSuccessfully() {
    when(tournamentRepository.findByName(creationRequest.getName())).thenReturn(Optional.empty());
    when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
    when(tournamentRepository.save(any(Tournament.class))).thenReturn(testTournament); // Mock para el guardado

    TournamentResponse result = tournamentService.createTournament(creationRequest, adminUser.getId());

    assertNotNull(result);
    assertEquals(testTournament.getName(), result.getName());
    assertEquals(testTournament.getMaxPlayers(), result.getMaxPlayers());
    assertEquals(TournamentStatus.CREATED, result.getStatus());
    assertEquals(adminUser.getId(), result.getCreator().getId()); // Verifica el creador
    verify(tournamentRepository, times(1)).findByName(creationRequest.getName());
    verify(userRepository, times(1)).findById(adminUser.getId());
    verify(tournamentRepository, times(1)).save(any(Tournament.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el nombre del torneo ya existe")
  void createTournament_shouldThrowException_whenNameAlreadyExists() {
    when(tournamentRepository.findByName(creationRequest.getName())).thenReturn(Optional.of(testTournament));

    assertThrows(IllegalArgumentException.class, () -> tournamentService.createTournament(creationRequest, adminUser.getId()));
    verify(tournamentRepository, times(1)).findByName(creationRequest.getName());
    verify(userRepository, never()).findById(anyLong()); // Verifica que no se llama a userRepository
    verify(tournamentRepository, never()).save(any(Tournament.class)); // Ni a save
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el creador no se encuentra")
  void createTournament_shouldThrowException_whenCreatorNotFound() {
    when(tournamentRepository.findByName(creationRequest.getName())).thenReturn(Optional.empty());
    when(userRepository.findById(adminUser.getId())).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> tournamentService.createTournament(creationRequest, adminUser.getId()));
    verify(tournamentRepository, times(1)).findByName(creationRequest.getName());
    verify(userRepository, times(1)).findById(adminUser.getId());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  // --- getAllTournaments Tests ---
  @Test
  @DisplayName("Debe retornar una lista de todos los torneos")
  void getAllTournaments_shouldReturnAllTournaments() {
    Tournament anotherTournament = new Tournament();
    anotherTournament.setId(11L);
    anotherTournament.setName("Another Tournament");
    anotherTournament.setMaxPlayers(2);
    anotherTournament.setCreator(player1);
    anotherTournament.setCreatedAt(LocalDateTime.now());
    anotherTournament.setStatus(TournamentStatus.CREATED);
    anotherTournament.setPlayers(new ArrayList<>());

    when(tournamentRepository.findAll()).thenReturn(Arrays.asList(testTournament, anotherTournament));

    List<TournamentResponse> result = tournamentService.getAllTournaments();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(testTournament.getName(), result.get(0).getName());
    assertEquals(anotherTournament.getName(), result.get(1).getName());
    verify(tournamentRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Debe retornar una lista vacía si no hay torneos")
  void getAllTournaments_shouldReturnEmptyList_whenNoTournaments() {
    when(tournamentRepository.findAll()).thenReturn(new ArrayList<>());

    List<TournamentResponse> result = tournamentService.getAllTournaments();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(tournamentRepository, times(1)).findAll();
  }

  // --- getTournamentById Tests ---
  @Test
  @DisplayName("Debe retornar un torneo por ID cuando existe")
  void getTournamentById_shouldReturnTournament_whenExists() {
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));

    TournamentResponse result = tournamentService.getTournamentById(testTournament.getId());

    assertNotNull(result);
    assertEquals(testTournament.getId(), result.getId());
    assertEquals(testTournament.getName(), result.getName());
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException al buscar torneo por ID que no existe")
  void getTournamentById_shouldThrowException_whenDoesNotExist() {
    Long nonExistentId = 999L;
    when(tournamentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> tournamentService.getTournamentById(nonExistentId));
    verify(tournamentRepository, times(1)).findById(nonExistentId);
  }

  // --- joinTournament Tests ---
  @Test
  @DisplayName("Debe permitir a un jugador unirse a un torneo exitosamente")
  void joinTournament_shouldAllowPlayerToJoinSuccessfully() {
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));
    when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Retorna el mismo objeto que se le pasó

    TournamentResponse result = tournamentService.joinTournament(testTournament.getId(), player1.getId());

    assertNotNull(result);
    assertTrue(result.getPlayers().stream().anyMatch(p -> p.getId().equals(player1.getId()))); // Verifica que el jugador está en la lista
    assertEquals(1, result.getPlayers().size()); // Ahora debería tener un jugador
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(userRepository, times(1)).findById(player1.getId());
    verify(tournamentRepository, times(1)).save(testTournament); // Verifica que el objeto tournament se guardó
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el torneo no se encuentra al unirse")
  void joinTournament_shouldThrowException_whenTournamentNotFound() {
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> tournamentService.joinTournament(testTournament.getId(), player1.getId()));
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(userRepository, never()).findById(anyLong());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el jugador no se encuentra al unirse")
  void joinTournament_shouldThrowException_whenPlayerNotFound() {
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(userRepository.findById(player1.getId())).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> tournamentService.joinTournament(testTournament.getId(), player1.getId()));
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(userRepository, times(1)).findById(player1.getId());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el torneo no está en estado 'CREATED'")
  void joinTournament_shouldThrowException_whenTournamentNotCreatedStatus() {
    testTournament.setStatus(TournamentStatus.IN_PROGRESS); // Cambia el estado del torneo de prueba
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));

    assertThrows(IllegalStateException.class, () -> tournamentService.joinTournament(testTournament.getId(), player1.getId()));
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(userRepository, times(1)).findById(player1.getId());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el torneo ya está lleno")
  void joinTournament_shouldThrowException_whenTournamentIsFull() {
    testTournament.setMaxPlayers(1); // Set max players to 1
    testTournament.getPlayers().add(player2); // Add player2 to fill it
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));

    assertThrows(IllegalStateException.class, () -> tournamentService.joinTournament(testTournament.getId(), player1.getId()));
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(userRepository, times(1)).findById(player1.getId());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el jugador ya está en el torneo")
  void joinTournament_shouldThrowException_whenPlayerAlreadyJoined() {
    testTournament.getPlayers().add(player1); // Add player1 already
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));

    assertThrows(IllegalArgumentException.class, () -> tournamentService.joinTournament(testTournament.getId(), player1.getId()));
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(userRepository, times(1)).findById(player1.getId());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }
}