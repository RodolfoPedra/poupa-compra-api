package br.com.poupacompra.integracao.common.converter.impl;

import org.springframework.stereotype.Component;

import br.com.poupacompra.integracao.common.converter.GenericConverter;
import br.com.poupacompra.integracao.dto.nota.NotaDTO;
import br.com.poupacompra.integracao.model.nota.GeralNota;

@Component
public class GeralNotaConverter implements GenericConverter<NotaDTO, GeralNota> {
  
    @Override
    public GeralNota dtoToEntity(NotaDTO dto) {
        return GeralNota.builder()
            .quantidadeItens(dto.getQuantidadeItens())
            .valorTotal(dto.getValorTotal())
            .ufCfe(dto.getUfCfe())
            .urlCfe(dto.getUrlCfe())
            .chaveAcesso(dto.getChaveAcesso())
            .build();
    }

    @Override
    public NotaDTO entityToDto(GeralNota entity) {
        return NotaDTO.builder()
            .quantidadeItens(entity.getQuantidadeItens())
            .valorTotal(entity.getValorTotal())
            .usuario(entity.getUsuario().getId())
            .ufCfe(entity.getUfCfe())
            .urlCfe(entity.getUrlCfe())
            .chaveAcesso(entity.getChaveAcesso())
            .build();
    }
}
