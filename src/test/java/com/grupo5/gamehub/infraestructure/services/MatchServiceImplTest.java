package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.matches.MatchGenerationRequest;
import com.grupo5.gamehub.api.dtos.matches.MatchResponse;
import com.grupo5.gamehub.api.dtos.matches.MatchResultUpdateRequest;
import com.grupo5.gamehub.api.dtos.matches.TournamentInMatchResponse;
import com.grupo5.gamehub.api.dtos.matches.UserInMatchResponse;
import com.grupo5.gamehub.domain.entities.Match;
import com.grupo5.gamehub.domain.entities.Tournament;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.enums.MatchStatus;
import com.grupo5.gamehub.domain.enums.Result;
import com.grupo5.gamehub.domain.enums.Role;
import com.grupo5.gamehub.domain.enums.TournamentStatus;
import com.grupo5.gamehub.domain.repositories.MatchRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceImplTest {

  @Mock
  private MatchRepository matchRepository;

  @Mock
  private TournamentRepository tournamentRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private MatchServiceImpl matchService;

  private User adminUser;
  private User player1;
  private User player2;
  private User player3;
  private User player4;
  private Tournament testTournament;
  private Match testMatch;

  @BeforeEach
  void setUp() {
    adminUser = new User();
    adminUser.setId(1L);
    adminUser.setUsername("adminUser");
    adminUser.setEmail("admin@example.com");
    adminUser.setRole(Role.ADMIN);
    adminUser.setPoints(0);
    adminUser.setRank(0);
    adminUser.setPassword("pass");

    player1 = new User();
    player1.setId(2L);
    player1.setUsername("player1");
    player1.setEmail("player1@example.com");
    player1.setRole(Role.PLAYER);
    player1.setPoints(100);
    player1.setRank(1);
    player1.setPassword("pass");

    player2 = new User();
    player2.setId(3L);
    player2.setUsername("player2");
    player2.setEmail("player2@example.com");
    player2.setRole(Role.PLAYER);
    player2.setPoints(90);
    player2.setRank(2);
    player2.setPassword("pass");

    player3 = new User();
    player3.setId(4L);
    player3.setUsername("player3");
    player3.setEmail("player3@example.com");
    player3.setRole(Role.PLAYER);
    player3.setPoints(80);
    player3.setRank(3);
    player3.setPassword("pass");

    player4 = new User();
    player4.setId(5L);
    player4.setUsername("player4");
    player4.setEmail("player4@example.com");
    player4.setRole(Role.PLAYER);
    player4.setPoints(70);
    player4.setRank(4);
    player4.setPassword("pass");

    testTournament = new Tournament();
    testTournament.setId(10L);
    testTournament.setName("Test Tournament");
    testTournament.setMaxPlayers(4);
    testTournament.setCreator(adminUser);
    testTournament.setCreatedAt(LocalDateTime.now());
    testTournament.setStatus(TournamentStatus.CREATED);
    testTournament.setPlayers(new ArrayList<>(Arrays.asList(player1, player2, player3, player4)));

    testMatch = new Match(testTournament, player1, player2, 1);
    testMatch.setId(100L);
    testMatch.setStatus(MatchStatus.PENDING);
    testMatch.setResult(Result.PENDING);
  }

  // --- generateMatches Tests ---

  @Test
  @DisplayName("Debe generar partidas exitosamente para un número par de jugadores")
  void generateMatches_shouldGenerateMatchesSuccessfully_evenPlayers() {
    // Preparar un torneo con 4 jugadores
    testTournament.setPlayers(new ArrayList<>(Arrays.asList(player1, player2, player3, player4)));
    testTournament.setStatus(TournamentStatus.CREATED); // Asegurar estado para el cambio a IN_PROGRESS

    MatchGenerationRequest request = new MatchGenerationRequest(1);

    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(matchRepository.findByTournamentAndRoundNumber(eq(testTournament), eq(request.getRoundNumber()))).thenReturn(Collections.emptyList());

    // Mockear saveAll para devolver las partidas con IDs simulados y asegurar que se pasen los argumentos correctos
    when(matchRepository.saveAll(anyList())).thenAnswer(invocation -> {
      List<Match> matchesToSave = invocation.getArgument(0);
      for (int i = 0; i < matchesToSave.size(); i++) {
        matchesToSave.get(i).setId((long) (100 + i)); // Asignar IDs simulados
      }
      return matchesToSave;
    });

    // Simular el guardado del torneo con el nuevo estado
    when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));


    List<MatchResponse> result = matchService.generateMatches(testTournament.getId(), request);

    assertNotNull(result);
    assertEquals(2, result.size()); // 4 players -> 2 matches

    // Verificar el estado de las partidas generadas
    result.forEach(match -> {
      assertNotNull(match.getId());
      assertEquals(MatchStatus.PENDING, match.getStatus());
      assertEquals(Result.PENDING, match.getResult());
      assertEquals(request.getRoundNumber(), match.getRoundNumber());
      assertNotNull(match.getPlayer1());
      assertNotNull(match.getPlayer2());
    });

    // Verificar que el estado del torneo cambió
    assertEquals(TournamentStatus.IN_PROGRESS, testTournament.getStatus());

    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(matchRepository, times(1)).findByTournamentAndRoundNumber(eq(testTournament), eq(request.getRoundNumber()));
    verify(matchRepository, times(1)).saveAll(anyList());
    verify(tournamentRepository, times(1)).save(testTournament); // Verifica que el estado del torneo se actualizó
  }

  @Test
  @DisplayName("Debe generar partidas exitosamente para un número impar de jugadores (con bye)")
  void generateMatches_shouldGenerateMatchesSuccessfully_oddPlayers() {
    // Preparar un torneo con 3 jugadores
    testTournament.setPlayers(new ArrayList<>(Arrays.asList(player1, player2, player3)));
    testTournament.setStatus(TournamentStatus.CREATED);

    MatchGenerationRequest request = new MatchGenerationRequest(1);

    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(matchRepository.findByTournamentAndRoundNumber(eq(testTournament), eq(request.getRoundNumber()))).thenReturn(Collections.emptyList());
    when(matchRepository.saveAll(anyList())).thenAnswer(invocation -> {
      List<Match> matchesToSave = invocation.getArgument(0);
      for (int i = 0; i < matchesToSave.size(); i++) {
        matchesToSave.get(i).setId((long) (200 + i));
      }
      return matchesToSave;
    });
    when(tournamentRepository.save(any(Tournament.class))).thenAnswer(invocation -> invocation.getArgument(0));


    List<MatchResponse> result = matchService.generateMatches(testTournament.getId(), request);

    assertNotNull(result);
    assertEquals(1, result.size()); // 3 players -> 1 match (1 bye)
    assertEquals(TournamentStatus.IN_PROGRESS, testTournament.getStatus());

    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(matchRepository, times(1)).findByTournamentAndRoundNumber(eq(testTournament), eq(request.getRoundNumber()));
    verify(matchRepository, times(1)).saveAll(anyList());
    verify(tournamentRepository, times(1)).save(testTournament);
  }

  @Test
  @DisplayName("Debe mantener el estado del torneo IN_PROGRESS si ya lo estaba")
  void generateMatches_shouldKeepTournamentStatusInProgress_ifAlreadyInProgress() {
    testTournament.setPlayers(new ArrayList<>(Arrays.asList(player1, player2)));
    testTournament.setStatus(TournamentStatus.IN_PROGRESS); // Estado ya en IN_PROGRESS

    MatchGenerationRequest request = new MatchGenerationRequest(2); // Segunda ronda

    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(matchRepository.findByTournamentAndRoundNumber(eq(testTournament), eq(request.getRoundNumber()))).thenReturn(Collections.emptyList());
    when(matchRepository.saveAll(anyList())).thenAnswer(invocation -> {
      List<Match> matchesToSave = invocation.getArgument(0);
      for (int i = 0; i < matchesToSave.size(); i++) {
        matchesToSave.get(i).setId((long) (300 + i));
      }
      return matchesToSave;
    });
    // No necesitamos mockear tournamentRepository.save() si el estado no cambia
    // Sin embargo, el servicio siempre llama a save si status es CREATED o lo cambia a IN_PROGRESS,
    // pero en este caso el `if` en el servicio evita el `save` si ya es IN_PROGRESS

    List<MatchResponse> result = matchService.generateMatches(testTournament.getId(), request);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(TournamentStatus.IN_PROGRESS, testTournament.getStatus()); // Verifica que se mantuvo

    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(matchRepository, times(1)).findByTournamentAndRoundNumber(eq(testTournament), eq(request.getRoundNumber()));
    verify(matchRepository, times(1)).saveAll(anyList());
    verify(tournamentRepository, never()).save(any(Tournament.class)); // El save no debería llamarse aquí
  }


  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el torneo no se encuentra")
  void generateMatches_shouldThrowException_whenTournamentNotFound() {
    Long nonExistentTournamentId = 999L;
    MatchGenerationRequest request = new MatchGenerationRequest(1);

    when(tournamentRepository.findById(nonExistentTournamentId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> matchService.generateMatches(nonExistentTournamentId, request));
    verify(tournamentRepository, times(1)).findById(nonExistentTournamentId);
    verify(matchRepository, never()).findByTournamentAndRoundNumber(any(), anyInt());
    verify(matchRepository, never()).saveAll(anyList());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el torneo tiene menos de 2 jugadores")
  void generateMatches_shouldThrowException_whenLessThanTwoPlayers() {
    testTournament.setPlayers(new ArrayList<>(Collections.singletonList(player1))); // Solo 1 jugador
    testTournament.setStatus(TournamentStatus.CREATED);

    MatchGenerationRequest request = new MatchGenerationRequest(1);

    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));

    assertThrows(IllegalStateException.class, () -> matchService.generateMatches(testTournament.getId(), request));
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(matchRepository, never()).findByTournamentAndRoundNumber(any(), anyInt());
    verify(matchRepository, never()).saveAll(anyList());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el número de ronda es nulo o inválido")
  void generateMatches_shouldThrowException_whenRoundNumberIsInvalid() {
    testTournament.setPlayers(new ArrayList<>(Arrays.asList(player1, player2)));
    testTournament.setStatus(TournamentStatus.CREATED);

    MatchGenerationRequest requestNull = new MatchGenerationRequest(null);
    MatchGenerationRequest requestZero = new MatchGenerationRequest(0);
    MatchGenerationRequest requestNegative = new MatchGenerationRequest(-1);

    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));

    assertThrows(IllegalArgumentException.class, () -> matchService.generateMatches(testTournament.getId(), requestNull));
    assertThrows(IllegalArgumentException.class, () -> matchService.generateMatches(testTournament.getId(), requestZero));
    assertThrows(IllegalArgumentException.class, () -> matchService.generateMatches(testTournament.getId(), requestNegative));

    verify(tournamentRepository, times(3)).findById(testTournament.getId()); // Se llama 3 veces por las 3 aserciones
    verify(matchRepository, never()).findByTournamentAndRoundNumber(any(), anyInt());
    verify(matchRepository, never()).saveAll(anyList());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el torneo ya está COMPLETED")
  void generateMatches_shouldThrowException_whenTournamentIsCompleted() {
    testTournament.setPlayers(new ArrayList<>(Arrays.asList(player1, player2)));
    testTournament.setStatus(TournamentStatus.COMPLETED); // Torneo completado

    MatchGenerationRequest request = new MatchGenerationRequest(1);

    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));

    assertThrows(IllegalStateException.class, () -> matchService.generateMatches(testTournament.getId(), request));
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(matchRepository, never()).findByTournamentAndRoundNumber(any(), anyInt());
    verify(matchRepository, never()).saveAll(anyList());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el torneo ya está CANCELLED")
  void generateMatches_shouldThrowException_whenTournamentIsCancelled() {
    testTournament.setPlayers(new ArrayList<>(Arrays.asList(player1, player2)));
    testTournament.setStatus(TournamentStatus.CANCELLED); // Torneo cancelado

    MatchGenerationRequest request = new MatchGenerationRequest(1);

    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));

    assertThrows(IllegalStateException.class, () -> matchService.generateMatches(testTournament.getId(), request));
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(matchRepository, never()).findByTournamentAndRoundNumber(any(), anyInt());
    verify(matchRepository, never()).saveAll(anyList());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si ya existen partidas para la ronda")
  void generateMatches_shouldThrowException_whenMatchesAlreadyExistForRound() {
    testTournament.setPlayers(new ArrayList<>(Arrays.asList(player1, player2)));
    testTournament.setStatus(TournamentStatus.CREATED);

    MatchGenerationRequest request = new MatchGenerationRequest(1);

    List<Match> existingMatches = Collections.singletonList(testMatch); // Simula que ya existe una partida
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(matchRepository.findByTournamentAndRoundNumber(eq(testTournament), eq(request.getRoundNumber()))).thenReturn(existingMatches);

    assertThrows(IllegalStateException.class, () -> matchService.generateMatches(testTournament.getId(), request));
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(matchRepository, times(1)).findByTournamentAndRoundNumber(eq(testTournament), eq(request.getRoundNumber()));
    verify(matchRepository, never()).saveAll(anyList());
    verify(tournamentRepository, never()).save(any(Tournament.class));
  }

  // --- getMatchById Tests ---

  @Test
  @DisplayName("Debe retornar una partida por ID cuando existe")
  void getMatchById_shouldReturnMatch_whenExists() {
    when(matchRepository.findById(testMatch.getId())).thenReturn(Optional.of(testMatch));

    MatchResponse result = matchService.getMatchById(testMatch.getId());

    assertNotNull(result);
    assertEquals(testMatch.getId(), result.getId());
    assertEquals(testMatch.getRoundNumber(), result.getRoundNumber());
    assertEquals(testMatch.getPlayer1().getUsername(), result.getPlayer1().getUsername());
    assertEquals(testMatch.getPlayer2().getUsername(), result.getPlayer2().getUsername());
    verify(matchRepository, times(1)).findById(testMatch.getId());
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException al buscar partida por ID que no existe")
  void getMatchById_shouldThrowException_whenDoesNotExist() {
    Long nonExistentMatchId = 9999L;
    when(matchRepository.findById(nonExistentMatchId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> matchService.getMatchById(nonExistentMatchId));
    verify(matchRepository, times(1)).findById(nonExistentMatchId);
  }

  // --- updateMatchResult Tests ---

  @Test
  @DisplayName("Debe actualizar el resultado de la partida a PLAYER1_WINS")
  void updateMatchResult_shouldUpdateResultToPlayer1Wins() {
    MatchResultUpdateRequest request = new MatchResultUpdateRequest(player1.getId(), Result.PLAYER1_WINS);

    // Clonar la partida para que el test no afecte otros si Mockito modifica el objeto real
    Match matchToUpdate = new Match(testTournament, player1, player2, 1);
    matchToUpdate.setId(testMatch.getId());
    matchToUpdate.setStatus(MatchStatus.PENDING);
    matchToUpdate.setResult(Result.PENDING);

    when(matchRepository.findById(matchToUpdate.getId())).thenReturn(Optional.of(matchToUpdate));
    when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));
    when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MatchResponse result = matchService.updateMatchResult(matchToUpdate.getId(), request);

    assertNotNull(result);
    assertEquals(MatchStatus.COMPLETED, result.getStatus());
    assertEquals(Result.PLAYER1_WINS, result.getResult());
    assertEquals(player1.getId(), result.getWinner().getId());

    verify(matchRepository, times(1)).findById(matchToUpdate.getId());
    verify(userRepository, times(1)).findById(player1.getId());
    verify(matchRepository, times(1)).save(matchToUpdate);
  }

  @Test
  @DisplayName("Debe actualizar el resultado de la partida a PLAYER2_WINS")
  void updateMatchResult_shouldUpdateResultToPlayer2Wins() {
    MatchResultUpdateRequest request = new MatchResultUpdateRequest(player2.getId(), Result.PLAYER2_WINS);

    Match matchToUpdate = new Match(testTournament, player1, player2, 1);
    matchToUpdate.setId(testMatch.getId());
    matchToUpdate.setStatus(MatchStatus.PENDING);
    matchToUpdate.setResult(Result.PENDING);

    when(matchRepository.findById(matchToUpdate.getId())).thenReturn(Optional.of(matchToUpdate));
    when(userRepository.findById(player2.getId())).thenReturn(Optional.of(player2));
    when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MatchResponse result = matchService.updateMatchResult(matchToUpdate.getId(), request);

    assertNotNull(result);
    assertEquals(MatchStatus.COMPLETED, result.getStatus());
    assertEquals(Result.PLAYER2_WINS, result.getResult());
    assertEquals(player2.getId(), result.getWinner().getId());

    verify(matchRepository, times(1)).findById(matchToUpdate.getId());
    verify(userRepository, times(1)).findById(player2.getId());
    verify(matchRepository, times(1)).save(matchToUpdate);
  }

  @Test
  @DisplayName("Debe actualizar el resultado de la partida a DRAW")
  void updateMatchResult_shouldUpdateResultToDraw() {
    MatchResultUpdateRequest request = new MatchResultUpdateRequest(null, Result.DRAW);

    Match matchToUpdate = new Match(testTournament, player1, player2, 1);
    matchToUpdate.setId(testMatch.getId());
    matchToUpdate.setStatus(MatchStatus.PENDING);
    matchToUpdate.setResult(Result.PENDING);

    when(matchRepository.findById(matchToUpdate.getId())).thenReturn(Optional.of(matchToUpdate));
    when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MatchResponse result = matchService.updateMatchResult(matchToUpdate.getId(), request);

    assertNotNull(result);
    assertEquals(MatchStatus.COMPLETED, result.getStatus());
    assertEquals(Result.DRAW, result.getResult());
    assertNull(result.getWinner()); // No debe haber ganador en caso de empate

    verify(matchRepository, times(1)).findById(matchToUpdate.getId());
    verify(userRepository, never()).findById(anyLong()); // No se busca ganador si es empate
    verify(matchRepository, times(1)).save(matchToUpdate);
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si la partida no se encuentra al actualizar resultado")
  void updateMatchResult_shouldThrowException_whenMatchNotFound() {
    Long nonExistentMatchId = 9999L;
    MatchResultUpdateRequest request = new MatchResultUpdateRequest(player1.getId(), Result.PLAYER1_WINS);

    when(matchRepository.findById(nonExistentMatchId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> matchService.updateMatchResult(nonExistentMatchId, request));
    verify(matchRepository, times(1)).findById(nonExistentMatchId);
    verify(userRepository, never()).findById(anyLong());
    verify(matchRepository, never()).save(any(Match.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si la partida ya está completada")
  void updateMatchResult_shouldThrowException_whenMatchAlreadyCompleted() {
    testMatch.setStatus(MatchStatus.COMPLETED); // Simular partida ya completada
    testMatch.setResult(Result.PLAYER1_WINS);
    testMatch.setWinner(player1);

    MatchResultUpdateRequest request = new MatchResultUpdateRequest(player2.getId(), Result.PLAYER2_WINS);

    when(matchRepository.findById(testMatch.getId())).thenReturn(Optional.of(testMatch));

    assertThrows(IllegalStateException.class, () -> matchService.updateMatchResult(testMatch.getId(), request));
    verify(matchRepository, times(1)).findById(testMatch.getId());
    verify(userRepository, never()).findById(anyLong());
    verify(matchRepository, never()).save(any(Match.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el resultado es PENDING en la actualización")
  void updateMatchResult_shouldThrowException_whenResultIsPending() {
    MatchResultUpdateRequest request = new MatchResultUpdateRequest(null, Result.PENDING); // Resultado PENDING

    Match matchToUpdate = new Match(testTournament, player1, player2, 1);
    matchToUpdate.setId(testMatch.getId());
    matchToUpdate.setStatus(MatchStatus.PENDING);
    matchToUpdate.setResult(Result.PENDING);

    when(matchRepository.findById(matchToUpdate.getId())).thenReturn(Optional.of(matchToUpdate));

    assertThrows(IllegalArgumentException.class, () -> matchService.updateMatchResult(matchToUpdate.getId(), request));
    verify(matchRepository, times(1)).findById(matchToUpdate.getId());
    verify(userRepository, never()).findById(anyLong());
    verify(matchRepository, never()).save(any(Match.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el ganador no se encuentra")
  void updateMatchResult_shouldThrowException_whenWinnerNotFound() {
    MatchResultUpdateRequest request = new MatchResultUpdateRequest(999L, Result.PLAYER1_WINS); // Ganador no existente

    Match matchToUpdate = new Match(testTournament, player1, player2, 1);
    matchToUpdate.setId(testMatch.getId());
    matchToUpdate.setStatus(MatchStatus.PENDING);
    matchToUpdate.setResult(Result.PENDING);

    when(matchRepository.findById(matchToUpdate.getId())).thenReturn(Optional.of(matchToUpdate));
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> matchService.updateMatchResult(matchToUpdate.getId(), request));
    verify(matchRepository, times(1)).findById(matchToUpdate.getId());
    verify(userRepository, times(1)).findById(999L);
    verify(matchRepository, never()).save(any(Match.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el ganador no es un jugador de la partida")
  void updateMatchResult_shouldThrowException_whenWinnerIsNotMatchPlayer() {
    User outsider = new User();
    outsider.setId(99L);
    outsider.setUsername("outsider");
    outsider.setEmail("outsider@example.com");
    outsider.setRole(Role.PLAYER);

    MatchResultUpdateRequest request = new MatchResultUpdateRequest(outsider.getId(), Result.PLAYER1_WINS);

    Match matchToUpdate = new Match(testTournament, player1, player2, 1);
    matchToUpdate.setId(testMatch.getId());
    matchToUpdate.setStatus(MatchStatus.PENDING);
    matchToUpdate.setResult(Result.PENDING);

    when(matchRepository.findById(matchToUpdate.getId())).thenReturn(Optional.of(matchToUpdate));
    when(userRepository.findById(outsider.getId())).thenReturn(Optional.of(outsider));

    assertThrows(IllegalArgumentException.class, () -> matchService.updateMatchResult(matchToUpdate.getId(), request));
    verify(matchRepository, times(1)).findById(matchToUpdate.getId());
    verify(userRepository, times(1)).findById(outsider.getId());
    verify(matchRepository, never()).save(any(Match.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el resultado es DRAW pero se especifica ganador")
  void updateMatchResult_shouldThrowException_whenResultIsDrawButWinnerProvided() {
    MatchResultUpdateRequest request = new MatchResultUpdateRequest(player1.getId(), Result.DRAW); // Draw con ganador

    Match matchToUpdate = new Match(testTournament, player1, player2, 1);
    matchToUpdate.setId(testMatch.getId());
    matchToUpdate.setStatus(MatchStatus.PENDING);
    matchToUpdate.setResult(Result.PENDING);

    when(matchRepository.findById(matchToUpdate.getId())).thenReturn(Optional.of(matchToUpdate));
    when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1)); // Necesario si se verifica antes de la excepción
    // Aunque la lógica del AssertTrue del DTO lo coge antes

    assertThrows(IllegalArgumentException.class, () -> matchService.updateMatchResult(matchToUpdate.getId(), request));
    verify(matchRepository, times(1)).findById(matchToUpdate.getId());
    verify(userRepository, times(1)).findById(player1.getId()); // Se llama si el servicio lo valida después de buscar el ganador
    verify(matchRepository, never()).save(any(Match.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el resultado no es DRAW pero no se especifica ganador")
  void updateMatchResult_shouldThrowException_whenResultNotDrawButNoWinnerProvided() {
    MatchResultUpdateRequest request = new MatchResultUpdateRequest(null, Result.PLAYER1_WINS); // Gana P1 pero winnerId es null

    Match matchToUpdate = new Match(testTournament, player1, player2, 1);
    matchToUpdate.setId(testMatch.getId());
    matchToUpdate.setStatus(MatchStatus.PENDING);
    matchToUpdate.setResult(Result.PENDING);

    when(matchRepository.findById(matchToUpdate.getId())).thenReturn(Optional.of(matchToUpdate));

    assertThrows(IllegalArgumentException.class, () -> matchService.updateMatchResult(matchToUpdate.getId(), request));
    verify(matchRepository, times(1)).findById(matchToUpdate.getId());
    verify(userRepository, never()).findById(anyLong()); // No se busca ganador
    verify(matchRepository, never()).save(any(Match.class));
  }

  // --- getMatchesByTournament Tests ---

  @Test
  @DisplayName("Debe retornar una lista de partidas para un torneo existente")
  void getMatchesByTournament_shouldReturnMatches_whenTournamentExists() {
    Match match2 = new Match(testTournament, player3, player4, 1);
    match2.setId(101L);
    match2.setStatus(MatchStatus.PENDING);
    match2.setResult(Result.PENDING);

    List<Match> matches = Arrays.asList(testMatch, match2);

    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(matchRepository.findByTournament(testTournament)).thenReturn(matches);

    List<MatchResponse> result = matchService.getMatchesByTournament(testTournament.getId());

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(testMatch.getId(), result.get(0).getId());
    assertEquals(match2.getId(), result.get(1).getId());

    // Verificar que los DTOs internos se mapean correctamente
    assertEquals(testTournament.getId(), result.get(0).getTournament().getId());
    assertEquals(player1.getId(), result.get(0).getPlayer1().getId());
    assertEquals(player2.getId(), result.get(0).getPlayer2().getId());

    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(matchRepository, times(1)).findByTournament(testTournament);
  }

  @Test
  @DisplayName("Debe retornar una lista vacía si el torneo existe pero no tiene partidas")
  void getMatchesByTournament_shouldReturnEmptyList_whenTournamentHasNoMatches() {
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(matchRepository.findByTournament(testTournament)).thenReturn(Collections.emptyList());

    List<MatchResponse> result = matchService.getMatchesByTournament(testTournament.getId());

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(matchRepository, times(1)).findByTournament(testTournament);
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el torneo no se encuentra al buscar partidas")
  void getMatchesByTournament_shouldThrowException_whenTournamentNotFound() {
    Long nonExistentTournamentId = 999L;
    when(tournamentRepository.findById(nonExistentTournamentId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> matchService.getMatchesByTournament(nonExistentTournamentId));
    verify(tournamentRepository, times(1)).findById(nonExistentTournamentId);
    verify(matchRepository, never()).findByTournament(any(Tournament.class));
  }
}