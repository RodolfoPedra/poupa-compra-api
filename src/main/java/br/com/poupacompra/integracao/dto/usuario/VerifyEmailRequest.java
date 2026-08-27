package br.com.poupacompra.integracao.dto.usuario;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(@NotBlank String token) {
}