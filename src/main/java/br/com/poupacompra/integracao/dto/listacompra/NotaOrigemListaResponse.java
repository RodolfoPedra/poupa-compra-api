package br.com.poupacompra.integracao.dto.listacompra;

import br.com.poupacompra.integracao.repository.NotaOrigemListaProjection;

public record NotaOrigemListaResponse(Long id, Integer numeroCfe, String dataHoraEmissao, Float valorTotal,
        Integer quantidadeItens) {
    public static NotaOrigemListaResponse from(NotaOrigemListaProjection projection) {
        return new NotaOrigemListaResponse(projection.getId(), projection.getNumeroCfe(),
                projection.getDataHoraEmissao(), projection.getValorTotal(), projection.getQuantidadeItens());
    }
}