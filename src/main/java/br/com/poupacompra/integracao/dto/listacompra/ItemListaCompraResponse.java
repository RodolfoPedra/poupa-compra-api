package br.com.poupacompra.integracao.dto.listacompra;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.poupacompra.integracao.model.listacompra.ItemListaCompra;
import br.com.poupacompra.integracao.model.listacompra.UnidadeMedida;

public record ItemListaCompraResponse(Long id, Long produtoId, String descricao, BigDecimal quantidade,
    UnidadeMedida unidade, boolean selecionado, int ordem, Instant updatedAt) {
    public static ItemListaCompraResponse from(ItemListaCompra item) {
        return new ItemListaCompraResponse(item.getId(), item.getProduto() == null ? null : item.getProduto().getId(),
        item.getDescricao(), item.getQuantidade(), item.getUnidade(), item.isSelecionado(), item.getOrdem(),
        item.getUpdatedAt());
    }
}