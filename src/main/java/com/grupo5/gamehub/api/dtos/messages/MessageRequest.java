package com.grupo5.gamehub.api.dtos.messages;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
  @NotBlank(message = "El contenido del mensaje no puede estar vacío.")
  @Size(max = 500, message = "El mensaje no puede exceder los 500 caracteres.")
  private String content;
}