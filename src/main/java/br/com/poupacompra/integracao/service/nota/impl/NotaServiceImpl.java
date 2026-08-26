package br.com.poupacompra.integracao.service.nota.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.poupacompra.integracao.common.converter.impl.EstabelecimentoConverter;
import br.com.poupacompra.integracao.common.converter.impl.GeralNotaConverter;
import br.com.poupacompra.integracao.common.converter.impl.ItensNotaConverter;
import br.com.poupacompra.integracao.dto.nota.NotaCompletaDTO;
import br.com.poupacompra.integracao.model.nota.Estabelecimento;
import br.com.poupacompra.integracao.model.nota.GeralNota;
import br.com.poupacompra.integracao.model.nota.ItensNota;
import br.com.poupacompra.integracao.repository.EstabelecimentoRepository;
import br.com.poupacompra.integracao.repository.NotaRepository;
import br.com.poupacompra.integracao.service.nota.NotaService;
import br.com.poupacompra.integracao.service.nota.validation.NotaValidation;

@Service
public class NotaServiceImpl implements NotaService {

  private final NotaRepository notaRepository;

  private final EstabelecimentoRepository estabelecimentoRepository;

  private final GeralNotaConverter geralNotaConverter;

  private final EstabelecimentoConverter estabelecimentoConverter;

  private final ItensNotaConverter itensNotaConverter;

  private final NotaValidation notaValidation;

  public NotaServiceImpl(NotaRepository notaRepository, EstabelecimentoRepository estabelecimentoRepository, GeralNotaConverter geralNotaConverter, EstabelecimentoConverter estabelecimentoConverter, ItensNotaConverter itensNotaConverter, NotaValidation notaValidation) {
    this.notaRepository = notaRepository;
    this.estabelecimentoRepository = estabelecimentoRepository;
    this.geralNotaConverter = geralNotaConverter;
    this.estabelecimentoConverter = estabelecimentoConverter;
    this.itensNotaConverter = itensNotaConverter;
    this.notaValidation = notaValidation;
  }

  @Override
  @Transactional
  public GeralNota salvarNota(NotaCompletaDTO notaDTO) {

    notaValidation.validarNotaExistente(notaDTO.getNota().getChaveAcesso());

    GeralNota geralNota = geralNotaConverter.dtoToEntity(notaDTO.getNota());
    Estabelecimento estabelecimento = estabelecimentoConverter.dtoToEntity(notaDTO.getEstabelecimento());
    List<ItensNota> itensNota =  itensNotaConverter.dtoToEntity(notaDTO.getItensNota());

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

  @Override
  public List<GeralNota> listarNotas() {
    return notaRepository.findAll();
  }

}
