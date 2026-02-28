package br.com.poupacompra.integracao.common.converter.impl;

import org.springframework.stereotype.Component;

import br.com.poupacompra.integracao.common.converter.GenericConverter;
import br.com.poupacompra.integracao.dto.nota.EstabelecimentoDTO;
import br.com.poupacompra.integracao.model.nota.Estabelecimento;

@Component
public class EstabelecimentoConverter implements GenericConverter<EstabelecimentoDTO, Estabelecimento> {
  
    @Override
    public Estabelecimento dtoToEntity(EstabelecimentoDTO dto) {
        return Estabelecimento.builder()
            .nomeEstabelecimento(dto.getNomeEstabelecimento())
            .cpfCnpj(dto.getCpfCnpj())
            .endereco(dto.getEndereco())
            .build();
    }

    @Override
    public EstabelecimentoDTO entityToDto(Estabelecimento entity) {
        return EstabelecimentoDTO.builder()
            .nomeEstabelecimento(entity.getNomeEstabelecimento())
            .cpfCnpj(entity.getCpfCnpj())
            .endereco(entity.getEndereco())
            .build();
    }
}
