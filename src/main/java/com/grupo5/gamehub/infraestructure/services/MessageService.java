package com.grupo5.gamehub.infraestructure.services;

import com.grupo5.gamehub.api.dtos.messages.MessageRequest;
import com.grupo5.gamehub.api.dtos.messages.MessageResponse;

import java.util.List;

public interface MessageService {
  // Para enviar un mensaje a un torneo
  MessageResponse sendMessageToTournament(Long tournamentId, Long senderId, MessageRequest messageRequest);

  // Para obtener mensajes de un torneo
  List<MessageResponse> getTournamentMessages(Long tournamentId);

  // Para enviar un mensaje a una partida
  MessageResponse sendMessageToMatch(Long matchId, Long senderId, MessageRequest messageRequest);

  // Para obtener mensajes de una partida
  List<MessageResponse> getMatchMessages(Long matchId);
}