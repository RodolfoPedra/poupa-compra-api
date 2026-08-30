package br.com.poupacompra.integracao.dto.listacompra;

import java.time.Instant;

import br.com.poupacompra.integracao.repository.ListaCompraResumoProjection;

public record ListaCompraResumoResponse(Long id, String nome, long quantidadeItens, long quantidadeSelecionados,
        Instant createdAt, Instant updatedAt) {
    public static ListaCompraResumoResponse from(ListaCompraResumoProjection lista) {
        return new ListaCompraResumoResponse(lista.getId(), lista.getNome(), lista.getQuantidadeItens(),
                lista.getQuantidadeSelecionados(), lista.getCreatedAt(), lista.getUpdatedAt());
    }
}