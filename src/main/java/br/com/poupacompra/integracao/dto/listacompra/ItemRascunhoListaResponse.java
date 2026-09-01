package br.com.poupacompra.integracao.dto.listacompra;

import java.math.BigDecimal;

import br.com.poupacompra.integracao.model.listacompra.UnidadeMedida;

public record ItemRascunhoListaResponse(Long produtoId, String descricao, BigDecimal quantidade,
        UnidadeMedida unidade, boolean selecionado, int ordem) {
    public static ItemRascunhoListaResponse from(String descricao, int ordem) {
        return new ItemRascunhoListaResponse(null, descricao, null, null, false, ordem);
    }
}