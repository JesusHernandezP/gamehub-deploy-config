package com.grupo5.gamehub.domain.enums;

public enum MatchStatus {
  PENDING,     // Partida programada, aún no jugada
  IN_PROGRESS, // la partida se está jugando
  COMPLETED,   // Partida terminada y resultado registrado
  CANCELED     // Partida cancelada
}