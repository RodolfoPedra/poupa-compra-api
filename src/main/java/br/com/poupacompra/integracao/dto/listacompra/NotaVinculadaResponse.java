package br.com.poupacompra.integracao.dto.listacompra;

import br.com.poupacompra.integracao.model.nota.GeralNota;

public record NotaVinculadaResponse(Long id, Integer numeroCfe, String dataHoraEmissao, Float valorTotal,
        Integer quantidadeItens) {
    public static NotaVinculadaResponse from(GeralNota nota) {
        return nota == null ? null : new NotaVinculadaResponse(nota.getId(), nota.getNumeroCfe(),
                nota.getDataHoraEmissao(), nota.getValorTotal(), nota.getQuantidadeItens());
    }
}