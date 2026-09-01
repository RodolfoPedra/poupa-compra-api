package br.com.poupacompra.integracao.dto.listacompra;

import java.time.Instant;

import br.com.poupacompra.integracao.repository.ListaCompartilhadaResumoProjection;

public record ListaCompartilhadaResumoResponse(Long id, String nome, String ownerNome,
        long quantidadeItens, long quantidadeSelecionados, Instant createdAt, Instant updatedAt) {
    public static ListaCompartilhadaResumoResponse from(ListaCompartilhadaResumoProjection projection) {
        return new ListaCompartilhadaResumoResponse(projection.getId(), projection.getNome(),
                projection.getOwnerNome(), projection.getQuantidadeItens(), projection.getQuantidadeSelecionados(),
                projection.getCreatedAt(), projection.getUpdatedAt());
    }
}