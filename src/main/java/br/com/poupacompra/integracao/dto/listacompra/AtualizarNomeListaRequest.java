package br.com.poupacompra.integracao.dto.listacompra;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarNomeListaRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotNull Instant updatedAt) {
}