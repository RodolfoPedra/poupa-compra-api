package br.com.poupacompra.integracao.service.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import br.com.poupacompra.integracao.config.ModelMapperConfig;
import br.com.poupacompra.integracao.dto.NotaCompletaDTO;
import br.com.poupacompra.integracao.model.Estabelecimento;
import br.com.poupacompra.integracao.model.GeralNota;
import br.com.poupacompra.integracao.model.ItensNota;
import br.com.poupacompra.integracao.repository.EstabelecimentoRepository;
import br.com.poupacompra.integracao.repository.NotaRepository;
import br.com.poupacompra.integracao.service.NotaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotaServiceImpl implements NotaService {

  private final ModelMapper mapper;

  private final NotaRepository notaRepository;

  private final EstabelecimentoRepository estabelecimentoRepository;

  @Override
  public GeralNota salvarNota(NotaCompletaDTO notaDTO) {
    GeralNota geralNota = mapper.map(notaDTO.getNota(), GeralNota.class);
    Estabelecimento estabelecimento = mapper.map(notaDTO.getEstabelecimento(), Estabelecimento.class);
    List<ItensNota> itensNota =  ModelMapperConfig.modelMapperList(notaDTO.getItensNota(), ItensNota.class);

    Estabelecimento currentEstabelecimento = estabelecimentoRepository.findByCpfCnpj(estabelecimento.getCpfCnpj())
      .orElse(estabelecimento);

    geralNota.setEstabelecimento(currentEstabelecimento);
    geralNota.setItensNotas(itensNota);

    return notaRepository.save(geralNota);
  }

}
