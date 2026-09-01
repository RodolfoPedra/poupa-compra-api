package br.com.poupacompra.integracao.repository;

import java.time.Instant;

public interface ListaCompartilhadaResumoProjection {
    Long getId();
    String getNome();
    String getOwnerNome();
    long getQuantidadeItens();
    long getQuantidadeSelecionados();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}