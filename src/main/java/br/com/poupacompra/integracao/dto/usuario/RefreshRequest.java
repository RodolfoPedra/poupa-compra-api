package br.com.poupacompra.integracao.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {
}