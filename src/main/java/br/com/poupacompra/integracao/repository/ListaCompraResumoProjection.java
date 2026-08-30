package br.com.poupacompra.integracao.repository;

import java.time.Instant;

public interface ListaCompraResumoProjection {
    Long getId();
    String getNome();
    long getQuantidadeItens();
    long getQuantidadeSelecionados();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}