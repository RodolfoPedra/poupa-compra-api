package br.com.poupacompra.integracao.dto.listacompra;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record AtualizarVinculoNotaRequest(Long notaId, @NotNull Instant updatedAt) {
}