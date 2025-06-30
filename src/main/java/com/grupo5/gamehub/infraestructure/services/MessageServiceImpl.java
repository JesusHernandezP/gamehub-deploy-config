package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.messages.MessageRequest;
import com.grupo5.gamehub.api.dtos.messages.MessageResponse;
import com.grupo5.gamehub.api.dtos.messages.UserMessageResponse;
import com.grupo5.gamehub.domain.entities.Message;
import com.grupo5.gamehub.domain.entities.Tournament;
import com.grupo5.gamehub.domain.entities.User;
import com.grupo5.gamehub.domain.entities.Match;
import com.grupo5.gamehub.domain.repositories.MessageRepository;
import com.grupo5.gamehub.domain.repositories.TournamentRepository;
import com.grupo5.gamehub.domain.repositories.UserRepository;
import com.grupo5.gamehub.domain.repositories.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

import com.grupo5.gamehub.domain.enums.Role;

@Service
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final TournamentRepository tournamentRepository;
  private final MatchRepository matchRepository;
  private final UserService userService;

  public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository,
                            TournamentRepository tournamentRepository, MatchRepository matchRepository,
                            UserService userService) {
    this.messageRepository = messageRepository;
    this.userRepository = userRepository;
    this.tournamentRepository = tournamentRepository;
    this.matchRepository = matchRepository;
    this.userService = userService;
  }

  private Long getAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
      throw new IllegalStateException("Usuario no autenticado.");
    }
    String username;
    if (authentication.getPrincipal() instanceof UserDetails) {
      username = ((UserDetails) authentication.getPrincipal()).getUsername();
    } else if (authentication.getPrincipal() instanceof String) {
      username = (String) authentication.getPrincipal();
    } else {
      throw new IllegalStateException("Formato de principal de autenticación desconocido.");
    }
    return userService.getUserIdByUsername(username);
  }

  @Override
  @Transactional
  public MessageResponse sendMessageToTournament(Long tournamentId, Long senderId, MessageRequest messageRequest) {
    User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("Sender no encontrado con ID: " + senderId));
    Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado con ID: " + tournamentId));

    if (!tournament.getPlayers().contains(sender) && sender.getRole() != Role.ADMIN) {
      throw new IllegalStateException("Solo los participantes del torneo o el administrador pueden enviar mensajes al chat del torneo.");
    }

    Message message = new Message(sender, messageRequest.getContent(), tournament);
    Message savedMessage = messageRepository.save(message);
    return mapToMessageResponse(savedMessage);
  }

  @Override
  public List<MessageResponse> getTournamentMessages(Long tournamentId) {
    Long authenticatedUserId = getAuthenticatedUserId();

    User authenticatedUser = userRepository.findById(authenticatedUserId)
            .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en el sistema."));

    Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado con ID: " + tournamentId));

    if (!tournament.getPlayers().contains(authenticatedUser) && authenticatedUser.getRole() != Role.ADMIN) {
      throw new IllegalStateException("Solo los participantes del torneo o el administrador pueden ver los mensajes del chat del torneo.");
    }

    return messageRepository.findByTournamentIdOrderBySentAtAsc(tournamentId).stream()
            .map(this::mapToMessageResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public MessageResponse sendMessageToMatch(Long matchId, Long senderId, MessageRequest messageRequest) {
    User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("Sender no encontrado con ID: " + senderId));
    Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada con ID: " + matchId));

    if (!match.getPlayer1().equals(sender) && !match.getPlayer2().equals(sender) && sender.getRole() != Role.ADMIN) {
      throw new IllegalStateException("Solo los jugadores de la partida o el administrador pueden enviar mensajes al chat de la partida.");
    }

    Message message = new Message(sender, messageRequest.getContent(), match);
    Message savedMessage = messageRepository.save(message);
    return mapToMessageResponse(savedMessage);
  }

  @Override
  public List<MessageResponse> getMatchMessages(Long matchId) {
    Long authenticatedUserId = getAuthenticatedUserId();
    User authenticatedUser = userRepository.findById(authenticatedUserId)
            .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en el sistema."));

    Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada con ID: " + matchId));

    // Verificar permisos
    if (!match.getPlayer1().equals(authenticatedUser) && !match.getPlayer2().equals(authenticatedUser) && authenticatedUser.getRole() != Role.ADMIN) {
      throw new IllegalStateException("Solo los jugadores de la partida o el administrador pueden ver los mensajes del chat de la partida.");
    }

    return messageRepository.findByMatchIdOrderBySentAtAsc(matchId).stream()
            .map(this::mapToMessageResponse)
            .collect(Collectors.toList());
  }

  private MessageResponse mapToMessageResponse(Message message) {
    return new MessageResponse(
            message.getId(),
            new UserMessageResponse(message.getSender().getId(), message.getSender().getUsername()),
            message.getContent(),
            message.getSentAt()
    );
  }
}