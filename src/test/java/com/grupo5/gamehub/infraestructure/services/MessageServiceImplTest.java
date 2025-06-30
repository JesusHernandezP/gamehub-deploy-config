package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.messages.MessageRequest;
import com.grupo5.gamehub.api.dtos.messages.MessageResponse;
import com.grupo5.gamehub.domain.entities.Match;
import com.grupo5.gamehub.domain.entities.Message;
import com.grupo5.gamehub.domain.entities.Tournament;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.enums.MatchStatus;
import com.grupo5.gamehub.domain.enums.Role;
import com.grupo5.gamehub.domain.enums.TournamentStatus;
import com.grupo5.gamehub.domain.repositories.MatchRepository;
import com.grupo5.gamehub.domain.repositories.MessageRepository;
import com.grupo5.gamehub.domain.repositories.TournamentRepository;
import com.grupo5.gamehub.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageServiceImplTest {

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TournamentRepository tournamentRepository;

  @Mock
  private MatchRepository matchRepository;

  @Mock
  private UserService userService;

  @InjectMocks
  private MessageServiceImpl messageService;

  private User adminUser;
  private User player1;
  private User player2;
  private User nonParticipantUser;
  private Tournament testTournament;
  private Match testMatch;
  private Message tournamentMessage;
  private Message matchMessage;

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

    nonParticipantUser = new User();
    nonParticipantUser.setId(4L);
    nonParticipantUser.setUsername("nonParticipant");
    nonParticipantUser.setEmail("nonpart@example.com");
    nonParticipantUser.setRole(Role.PLAYER);
    nonParticipantUser.setPassword("pass");

    testTournament = new Tournament();
    testTournament.setId(10L);
    testTournament.setName("Test Tournament");
    testTournament.setMaxPlayers(4);
    testTournament.setCreator(adminUser);
    testTournament.setCreatedAt(LocalDateTime.now());
    testTournament.setStatus(TournamentStatus.IN_PROGRESS);
    testTournament.setPlayers(new ArrayList<>(Arrays.asList(player1, player2)));

    testMatch = new Match();
    testMatch.setId(100L);
    testMatch.setTournament(testTournament);
    testMatch.setPlayer1(player1);
    testMatch.setPlayer2(player2);
    testMatch.setStatus(MatchStatus.PENDING);
    testMatch.setMessages(new ArrayList<>());

    tournamentMessage = new Message(player1, "Hello Tournament!", testTournament);
    tournamentMessage.setId(200L);
    tournamentMessage.setSentAt(LocalDateTime.now());

    matchMessage = new Message(player1, "Hello Match!", testMatch);
    matchMessage.setId(201L);
    matchMessage.setSentAt(LocalDateTime.now());
  }

  // Helper para mockear SecurityContextHolder para usuarios AUTENTICADOS
  // Este método NO debe usarse para el test de "no autenticado".
  private MockedStatic<SecurityContextHolder> mockSecurityContext(Long authenticatedUserId, String username, Role role) {
    MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class);

    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);
    UserDetails userDetails = mock(UserDetails.class);

    mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true); // Siempre autenticado
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userDetails.getUsername()).thenReturn(username);

    // Mock para el userService, ya que getAuthenticatedUserId() lo llama.
    when(userService.getUserIdByUsername(username)).thenReturn(authenticatedUserId);
    User authenticatedUser = new User();
    authenticatedUser.setId(authenticatedUserId);
    authenticatedUser.setUsername(username);
    authenticatedUser.setRole(role);
    authenticatedUser.setEmail(username + "@example.com");
    authenticatedUser.setPassword("password");

    when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(authenticatedUser));

    return mockedStatic;
  }

  // --- sendMessageToTournament Tests ---

  @Test
  @DisplayName("Debe enviar un mensaje a un torneo por un jugador participante")
  void sendMessageToTournament_shouldSendMessage_byParticipant() {
    MessageRequest request = new MessageRequest("Test message from player.");

    when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));

    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
      Message msg = invocation.getArgument(0);
      msg.setId(300L);
      msg.setSentAt(LocalDateTime.now());
      return msg;
    });

    MessageResponse response = messageService.sendMessageToTournament(testTournament.getId(), player1.getId(), request);

    assertNotNull(response);
    assertEquals(request.getContent(), response.getContent());
    assertEquals(player1.getId(), response.getSender().getId());
    assertEquals(player1.getUsername(), response.getSender().getUsername());
    assertNotNull(response.getSentAt());

    verify(userRepository, times(1)).findById(player1.getId());
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  @DisplayName("Debe enviar un mensaje a un torneo por un ADMIN")
  void sendMessageToTournament_shouldSendMessage_byAdmin() {
    MessageRequest request = new MessageRequest("Test message from admin.");

    when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
      Message msg = invocation.getArgument(0);
      msg.setId(301L);
      msg.setSentAt(LocalDateTime.now());
      return msg;
    });

    MessageResponse response = messageService.sendMessageToTournament(testTournament.getId(), adminUser.getId(), request);

    assertNotNull(response);
    assertEquals(request.getContent(), response.getContent());
    assertEquals(adminUser.getId(), response.getSender().getId());
    assertEquals(adminUser.getUsername(), response.getSender().getUsername());
    assertNotNull(response.getSentAt());

    verify(userRepository, times(1)).findById(adminUser.getId());
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el remitente no se encuentra al enviar a torneo")
  void sendMessageToTournament_shouldThrowException_whenSenderNotFound() {
    MessageRequest request = new MessageRequest("Test message.");
    Long nonExistentSenderId = 99L;

    when(userRepository.findById(nonExistentSenderId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> messageService.sendMessageToTournament(testTournament.getId(), nonExistentSenderId, request));
    verify(userRepository, times(1)).findById(nonExistentSenderId);
    verify(tournamentRepository, never()).findById(anyLong());
    verify(messageRepository, never()).save(any(Message.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el torneo no se encuentra al enviar a torneo")
  void sendMessageToTournament_shouldThrowException_whenTournamentNotFound() {
    MessageRequest request = new MessageRequest("Test message.");
    Long nonExistentTournamentId = 99L;

    when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));
    when(tournamentRepository.findById(nonExistentTournamentId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> messageService.sendMessageToTournament(nonExistentTournamentId, player1.getId(), request));
    verify(userRepository, times(1)).findById(player1.getId());
    verify(tournamentRepository, times(1)).findById(nonExistentTournamentId);
    verify(messageRepository, never()).save(any(Message.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el remitente no es participante ni ADMIN al enviar a torneo")
  void sendMessageToTournament_shouldThrowException_whenSenderNotParticipantOrAdmin() {
    MessageRequest request = new MessageRequest("Test message.");

    testTournament.setPlayers(new ArrayList<>(Collections.singletonList(player1)));

    when(userRepository.findById(nonParticipantUser.getId())).thenReturn(Optional.of(nonParticipantUser));
    when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));

    assertThrows(IllegalStateException.class, () -> messageService.sendMessageToTournament(testTournament.getId(), nonParticipantUser.getId(), request));
    verify(userRepository, times(1)).findById(nonParticipantUser.getId());
    verify(tournamentRepository, times(1)).findById(testTournament.getId());
    verify(messageRepository, never()).save(any(Message.class));
  }

  // --- getTournamentMessages Tests ---

  @Test
  @DisplayName("Debe obtener mensajes de un torneo para un jugador participante")
  void getTournamentMessages_shouldReturnMessages_forParticipant() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockSecurityContext(player1.getId(), player1.getUsername(), player1.getRole())) {
      List<Message> messages = Arrays.asList(tournamentMessage, new Message(player2, "Another msg", testTournament));
      messages.get(1).setId(202L);
      messages.get(1).setSentAt(LocalDateTime.now().plusMinutes(1));

      when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
      when(messageRepository.findByTournamentIdOrderBySentAtAsc(testTournament.getId())).thenReturn(messages);

      List<MessageResponse> result = messageService.getTournamentMessages(testTournament.getId());

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(tournamentMessage.getId(), result.get(0).getId());
      assertEquals(player1.getId(), result.get(0).getSender().getId());
      assertEquals(player2.getId(), result.get(1).getSender().getId());

      verify(tournamentRepository, times(1)).findById(testTournament.getId());
      verify(messageRepository, times(1)).findByTournamentIdOrderBySentAtAsc(testTournament.getId());
    }
  }

  @Test
  @DisplayName("Debe obtener mensajes de un torneo para un ADMIN")
  void getTournamentMessages_shouldReturnMessages_forAdmin() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockSecurityContext(adminUser.getId(), adminUser.getUsername(), adminUser.getRole())) {
      List<Message> messages = Arrays.asList(tournamentMessage);

      when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));
      when(messageRepository.findByTournamentIdOrderBySentAtAsc(testTournament.getId())).thenReturn(messages);

      List<MessageResponse> result = messageService.getTournamentMessages(testTournament.getId());

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(tournamentMessage.getId(), result.get(0).getId());

      verify(tournamentRepository, times(1)).findById(testTournament.getId());
      verify(messageRepository, times(1)).findByTournamentIdOrderBySentAtAsc(testTournament.getId());
    }
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el torneo no se encuentra al obtener mensajes de torneo")
  void getTournamentMessages_shouldThrowException_whenTournamentNotFound() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockSecurityContext(player1.getId(), player1.getUsername(), player1.getRole())) {
      Long nonExistentTournamentId = 99L;
      when(tournamentRepository.findById(nonExistentTournamentId)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () -> messageService.getTournamentMessages(nonExistentTournamentId));
      verify(tournamentRepository, times(1)).findById(nonExistentTournamentId);
      verify(messageRepository, never()).findByTournamentIdOrderBySentAtAsc(anyLong());
    }
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el usuario autenticado no es participante ni ADMIN al obtener mensajes de torneo")
  void getTournamentMessages_shouldThrowException_whenUserNotParticipantOrAdmin() {
    testTournament.setPlayers(new ArrayList<>(Collections.singletonList(player1))); // Solo player1 es participante

    try (MockedStatic<SecurityContextHolder> mockedStatic = mockSecurityContext(nonParticipantUser.getId(), nonParticipantUser.getUsername(), nonParticipantUser.getRole())) {
      when(tournamentRepository.findById(testTournament.getId())).thenReturn(Optional.of(testTournament));

      assertThrows(IllegalStateException.class, () -> messageService.getTournamentMessages(testTournament.getId()));
      verify(tournamentRepository, times(1)).findById(testTournament.getId());
      verify(messageRepository, never()).findByTournamentIdOrderBySentAtAsc(anyLong());
    }
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el usuario autenticado no es encontrado al obtener mensajes de torneo")
  void getTournamentMessages_shouldThrowException_whenAuthenticatedUserNotFound() {
    Long nonExistentUserId = 99L;
    String username = "nonExistentUser";

    try (MockedStatic<SecurityContextHolder> mockedStatic = mockSecurityContext(nonExistentUserId, username, Role.PLAYER)) { // Usar un ID que no exista en tus mocks de usuario

      when(userService.getUserIdByUsername(username)).thenReturn(nonExistentUserId);
      when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty()); // ESTO ES CLAVE: No se encuentra el usuario
      assertThrows(IllegalStateException.class, () -> messageService.getTournamentMessages(testTournament.getId()));
      verify(userRepository, times(1)).findById(nonExistentUserId);
      verify(tournamentRepository, never()).findById(anyLong());

      verify(messageRepository, never()).findByTournamentIdOrderBySentAtAsc(anyLong());

    }
  }

  // --- sendMessageToMatch Tests ---

  @Test
  @DisplayName("Debe enviar un mensaje a una partida por un jugador de la partida")
  void sendMessageToMatch_shouldSendMessage_byMatchPlayer() {
    MessageRequest request = new MessageRequest("Test message from player in match.");

    when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));
    when(matchRepository.findById(testMatch.getId())).thenReturn(Optional.of(testMatch));
    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
      Message msg = invocation.getArgument(0);
      msg.setId(400L);
      msg.setSentAt(LocalDateTime.now());
      return msg;
    });

    MessageResponse response = messageService.sendMessageToMatch(testMatch.getId(), player1.getId(), request);

    assertNotNull(response);
    assertEquals(request.getContent(), response.getContent());
    assertEquals(player1.getId(), response.getSender().getId());
    assertEquals(player1.getUsername(), response.getSender().getUsername());
    assertNotNull(response.getSentAt());

    verify(userRepository, times(1)).findById(player1.getId());
    verify(matchRepository, times(1)).findById(testMatch.getId());
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  @DisplayName("Debe enviar un mensaje a una partida por un ADMIN")
  void sendMessageToMatch_shouldSendMessage_byAdmin() {
    MessageRequest request = new MessageRequest("Test message from admin in match.");

    when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
    when(matchRepository.findById(testMatch.getId())).thenReturn(Optional.of(testMatch));
    when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
      Message msg = invocation.getArgument(0);
      msg.setId(401L);
      msg.setSentAt(LocalDateTime.now());
      return msg;
    });

    MessageResponse response = messageService.sendMessageToMatch(testMatch.getId(), adminUser.getId(), request);

    assertNotNull(response);
    assertEquals(request.getContent(), response.getContent());
    assertEquals(adminUser.getId(), response.getSender().getId());
    assertEquals(adminUser.getUsername(), response.getSender().getUsername());
    assertNotNull(response.getSentAt());

    verify(userRepository, times(1)).findById(adminUser.getId());
    verify(matchRepository, times(1)).findById(testMatch.getId());
    verify(messageRepository, times(1)).save(any(Message.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si el remitente no se encuentra al enviar a partida")
  void sendMessageToMatch_shouldThrowException_whenSenderNotFound() {
    MessageRequest request = new MessageRequest("Test message.");
    Long nonExistentSenderId = 99L;

    when(userRepository.findById(nonExistentSenderId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> messageService.sendMessageToMatch(testMatch.getId(), nonExistentSenderId, request));
    verify(userRepository, times(1)).findById(nonExistentSenderId);
    verify(matchRepository, never()).findById(anyLong());
    verify(messageRepository, never()).save(any(Message.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si la partida no se encuentra al enviar a partida")
  void sendMessageToMatch_shouldThrowException_whenMatchNotFound() {
    MessageRequest request = new MessageRequest("Test message.");
    Long nonExistentMatchId = 99L;

    when(userRepository.findById(player1.getId())).thenReturn(Optional.of(player1));
    when(matchRepository.findById(nonExistentMatchId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> messageService.sendMessageToMatch(nonExistentMatchId, player1.getId(), request));
    verify(userRepository, times(1)).findById(player1.getId());
    verify(matchRepository, times(1)).findById(nonExistentMatchId);
    verify(messageRepository, never()).save(any(Message.class));
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el remitente no es jugador de la partida ni ADMIN al enviar a partida")
  void sendMessageToMatch_shouldThrowException_whenSenderNotMatchPlayerOrAdmin() {
    MessageRequest request = new MessageRequest("Test message.");

    when(userRepository.findById(nonParticipantUser.getId())).thenReturn(Optional.of(nonParticipantUser));
    when(matchRepository.findById(testMatch.getId())).thenReturn(Optional.of(testMatch));

    assertThrows(IllegalStateException.class, () -> messageService.sendMessageToMatch(testMatch.getId(), nonParticipantUser.getId(), request));
    verify(userRepository, times(1)).findById(nonParticipantUser.getId());
    verify(matchRepository, times(1)).findById(testMatch.getId());
    verify(messageRepository, never()).save(any(Message.class));
  }

  // --- getMatchMessages Tests ---

  @Test
  @DisplayName("Debe obtener mensajes de una partida para un jugador de la partida")
  void getMatchMessages_shouldReturnMessages_forMatchPlayer() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockSecurityContext(player1.getId(), player1.getUsername(), player1.getRole())) {
      List<Message> messages = Arrays.asList(matchMessage, new Message(player2, "Another match msg", testMatch));
      messages.get(1).setId(402L);
      messages.get(1).setSentAt(LocalDateTime.now().plusMinutes(1));

      when(matchRepository.findById(testMatch.getId())).thenReturn(Optional.of(testMatch));
      when(messageRepository.findByMatchIdOrderBySentAtAsc(testMatch.getId())).thenReturn(messages);

      List<MessageResponse> result = messageService.getMatchMessages(testMatch.getId());

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(matchMessage.getId(), result.get(0).getId());
      assertEquals(player1.getId(), result.get(0).getSender().getId());
      assertEquals(player2.getId(), result.get(1).getSender().getId());

      verify(matchRepository, times(1)).findById(testMatch.getId());
      verify(messageRepository, times(1)).findByMatchIdOrderBySentAtAsc(testMatch.getId());
    }
  }

  @Test
  @DisplayName("Debe obtener mensajes de una partida para un ADMIN")
  void getMatchMessages_shouldReturnMessages_forAdmin() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockSecurityContext(adminUser.getId(), adminUser.getUsername(), adminUser.getRole())) {
      List<Message> messages = Arrays.asList(matchMessage);

      when(matchRepository.findById(testMatch.getId())).thenReturn(Optional.of(testMatch));
      when(messageRepository.findByMatchIdOrderBySentAtAsc(testMatch.getId())).thenReturn(messages);

      List<MessageResponse> result = messageService.getMatchMessages(testMatch.getId());

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(matchMessage.getId(), result.get(0).getId());

      verify(matchRepository, times(1)).findById(testMatch.getId());
      verify(messageRepository, times(1)).findByMatchIdOrderBySentAtAsc(testMatch.getId());
    }
  }

  @Test
  @DisplayName("Debe lanzar IllegalArgumentException si la partida no se encuentra al obtener mensajes de partida")
  void getMatchMessages_shouldThrowException_whenMatchNotFound() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockSecurityContext(player1.getId(), player1.getUsername(), player1.getRole())) {
      Long nonExistentMatchId = 99L;
      when(matchRepository.findById(nonExistentMatchId)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () -> messageService.getMatchMessages(nonExistentMatchId));
      verify(matchRepository, times(1)).findById(nonExistentMatchId);
      verify(messageRepository, never()).findByMatchIdOrderBySentAtAsc(anyLong());
    }
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el usuario autenticado no es jugador de la partida ni ADMIN al obtener mensajes de partida")
  void getMatchMessages_shouldThrowException_whenUserNotMatchPlayerOrAdmin() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockSecurityContext(nonParticipantUser.getId(), nonParticipantUser.getUsername(), nonParticipantUser.getRole())) {
      when(matchRepository.findById(testMatch.getId())).thenReturn(Optional.of(testMatch));

      assertThrows(IllegalStateException.class, () -> messageService.getMatchMessages(testMatch.getId()));
      verify(matchRepository, times(1)).findById(testMatch.getId());
      verify(messageRepository, never()).findByMatchIdOrderBySentAtAsc(anyLong());
    }
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si el usuario autenticado no es encontrado al obtener mensajes de partida")
  void getMatchMessages_shouldThrowException_whenAuthenticatedUserNotFound() {
    Long nonExistentUserId = 99L;
    String username = "nonExistentUser";

    try (MockedStatic<SecurityContextHolder> mockedStatic = mockSecurityContext(nonExistentUserId, username, Role.PLAYER)) {

      when(userService.getUserIdByUsername(username)).thenReturn(nonExistentUserId);
      when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());
      assertThrows(IllegalStateException.class, () -> messageService.getMatchMessages(testMatch.getId()));
      verify(userRepository, times(1)).findById(nonExistentUserId);
      verify(messageRepository, never()).findByMatchIdOrderBySentAtAsc(anyLong());
    }
  }

  @Test
  @DisplayName("Debe lanzar IllegalStateException si no hay usuario autenticado al intentar obtener mensajes")
  void getMessages_shouldThrowException_whenNotAuthenticated() {
    try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
      SecurityContext securityContext = mock(SecurityContext.class);
      Authentication authentication = mock(Authentication.class);
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(false);
      when(authentication.getPrincipal()).thenReturn("anonymousUser");

      IllegalStateException tournamentException = assertThrows(IllegalStateException.class,
              () -> messageService.getTournamentMessages(testTournament.getId()),
              "Debe lanzar IllegalStateException para mensajes de torneo cuando no hay usuario autenticado.");

      assertEquals("Usuario no autenticado.", tournamentException.getMessage(),
              "El mensaje de la excepción de torneo no autenticado debe ser 'Usuario no autenticado.'.");

      verify(tournamentRepository, never()).findById(anyLong());


      // --- Prueba para getMatchMessages ---
      IllegalStateException matchException = assertThrows(IllegalStateException.class,
              () -> messageService.getMatchMessages(testMatch.getId()),
              "Debe lanzar IllegalStateException para mensajes de partida cuando no hay usuario autenticado.");
      assertEquals("Usuario no autenticado.", matchException.getMessage(),
              "El mensaje de la excepción de partida no autenticada debe ser 'Usuario no autenticado.'.");
      verify(matchRepository, never()).findById(anyLong());

    }
  }
}