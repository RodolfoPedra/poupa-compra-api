package br.com.poupacompra.integracao.dto.listacompra;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.poupacompra.integracao.model.listacompra.UnidadeMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarItemListaRequest(
        @Size(max = 160) String descricao,
        @DecimalMin(value = "0.001") @Digits(integer = 9, fraction = 3) BigDecimal quantidade,
        UnidadeMedida unidade,
        @NotNull Instant updatedAt) {
}