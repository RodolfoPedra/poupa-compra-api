package br.com.poupacompra.integracao.common.converter.impl;

import org.springframework.stereotype.Component;

import br.com.poupacompra.integracao.common.converter.ListGenericConverter;
import br.com.poupacompra.integracao.dto.nota.ItensNotaDTO;
import br.com.poupacompra.integracao.model.nota.ItensNota;

@Component
public class ItensNotaConverter implements ListGenericConverter<ItensNotaDTO, ItensNota> {
  
    @Override
    public ItensNota dtoToEntity(ItensNotaDTO dto) {
        return ItensNota.builder()
            .descricao(dto.getDescricao())
            .quantidade(dto.getQuantidade())
            .tipoUnidade(dto.getTipoUnidade())
            .valorUnitario(dto.getValorUnitario())
            .valorTotal(dto.getValorTotal())
            .build();
    }

    @Override
    public ItensNotaDTO entityToDto(ItensNota entity) {
        return ItensNotaDTO.builder()
            .descricao(entity.getDescricao())
            .quantidade(entity.getQuantidade())
            .tipoUnidade(entity.getTipoUnidade())
            .valorUnitario(entity.getValorUnitario())
            .valorTotal(entity.getValorTotal())
            .build();
    }
}
