package br.com.poupacompra.integracao.dto.listacompra;

import br.com.poupacompra.integracao.repository.EstabelecimentoNotaProjection;

public record EstabelecimentoNotaResponse(Long id, String nome, String cpfCnpj) {
    public static EstabelecimentoNotaResponse from(EstabelecimentoNotaProjection projection) {
        return new EstabelecimentoNotaResponse(projection.getId(), projection.getNome(), projection.getCpfCnpj());
    }
}