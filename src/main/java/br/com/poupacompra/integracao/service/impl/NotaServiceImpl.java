package br.com.poupacompra.integracao.service.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.poupacompra.integracao.config.ModelMapperConfig;
import br.com.poupacompra.integracao.dto.nota.NotaCompletaDTO;
import br.com.poupacompra.integracao.model.nota.Estabelecimento;
import br.com.poupacompra.integracao.model.nota.GeralNota;
import br.com.poupacompra.integracao.model.nota.ItensNota;
import br.com.poupacompra.integracao.repository.EstabelecimentoRepository;
import br.com.poupacompra.integracao.repository.NotaRepository;
import br.com.poupacompra.integracao.service.NotaService;

@Service
public class NotaServiceImpl implements NotaService {

  private final ModelMapper mapper;

  private final NotaRepository notaRepository;

  private final EstabelecimentoRepository estabelecimentoRepository;

  public NotaServiceImpl(ModelMapper mapper, NotaRepository notaRepository, EstabelecimentoRepository estabelecimentoRepository) {
    this.mapper = mapper;
    this.notaRepository = notaRepository;
    this.estabelecimentoRepository = estabelecimentoRepository;
  }

  @Override
  @Transactional
  public GeralNota salvarNota(NotaCompletaDTO notaDTO) {
    GeralNota geralNota = mapper.map(notaDTO.getNota(), GeralNota.class);
    Estabelecimento estabelecimento = mapper.map(notaDTO.getEstabelecimento(), Estabelecimento.class);
    List<ItensNota> itensNota =  ModelMapperConfig.modelMapperList(notaDTO.getItensNota(), ItensNota.class);

    var optEst = estabelecimentoRepository.findByCpfCnpj(estabelecimento.getCpfCnpj());
    Estabelecimento currentEstabelecimento;
    if (optEst.isPresent()) {
      currentEstabelecimento = optEst.get();
    } else {
      currentEstabelecimento = estabelecimentoRepository.save(estabelecimento);
    }

    geralNota.setEstabelecimento(currentEstabelecimento);
    geralNota.setItensNotas(itensNota);

    return notaRepository.save(geralNota);
  }

}
