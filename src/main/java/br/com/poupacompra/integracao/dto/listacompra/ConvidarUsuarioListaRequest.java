package br.com.poupacompra.integracao.dto.listacompra;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConvidarUsuarioListaRequest(
        @NotBlank @Email String email,
        @NotNull Instant updatedAt) {
}