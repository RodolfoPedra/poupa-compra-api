package br.com.poupacompra.integracao.dto.listacompra;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalvarListaCompraRequest(
        @NotBlank(message = "Nome da lista é obrigatório")
        @Size(max = 120, message = "Nome da lista deve ter no máximo 120 caracteres") String nome,
        Instant updatedAt,
        @NotNull(message = "Itens são obrigatórios") List<@Valid SalvarItemListaCompraRequest> itens) {
}