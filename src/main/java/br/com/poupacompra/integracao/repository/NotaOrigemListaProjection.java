package br.com.poupacompra.integracao.repository;

public interface NotaOrigemListaProjection {
    Long getId();

    Integer getNumeroCfe();

    String getDataHoraEmissao();

    Float getValorTotal();

    Integer getQuantidadeItens();
}