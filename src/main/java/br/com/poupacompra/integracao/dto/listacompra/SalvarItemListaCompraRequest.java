package br.com.poupacompra.integracao.dto.listacompra;

import java.math.BigDecimal;

import br.com.poupacompra.integracao.model.listacompra.UnidadeMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalvarItemListaCompraRequest(
        Long id,
        Long produtoId,
        @Size(max = 160, message = "Descrição deve ter no máximo 160 caracteres") String descricao,
        @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero")
        @Digits(integer = 9, fraction = 3, message = "Quantidade deve ter até 9 inteiros e 3 decimais")
        BigDecimal quantidade,
        UnidadeMedida unidade,
        boolean selecionado,
        @NotNull(message = "Ordem é obrigatória") @Min(value = 0, message = "Ordem não pode ser negativa") Integer ordem) {
}