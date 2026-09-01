package br.com.poupacompra.integracao.dto.listacompra;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GerarRascunhoListaRequest(
        @NotEmpty(message = "Selecione ao menos uma nota")
        @Size(max = 5, message = "Selecione no máximo 5 notas")
        List<@NotNull(message = "Nota inválida") Long> notaIds) {
}