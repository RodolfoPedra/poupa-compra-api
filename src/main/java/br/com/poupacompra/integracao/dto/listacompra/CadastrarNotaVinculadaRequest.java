package br.com.poupacompra.integracao.dto.listacompra;

import java.time.Instant;

import br.com.poupacompra.integracao.dto.nota.NotaCompletaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CadastrarNotaVinculadaRequest(
        @NotNull Instant updatedAt,
        @Valid @NotNull NotaCompletaDTO nota) {
}