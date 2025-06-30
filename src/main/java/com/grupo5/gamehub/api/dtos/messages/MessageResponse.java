package com.grupo5.gamehub.api.dtos.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
  private Long id;
  private UserMessageResponse sender;
  private String content;
  private LocalDateTime sentAt;
}