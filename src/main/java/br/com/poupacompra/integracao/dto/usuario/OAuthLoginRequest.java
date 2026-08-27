package br.com.poupacompra.integracao.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(@NotBlank String authorizationCode, @NotBlank String redirectUri) {
}