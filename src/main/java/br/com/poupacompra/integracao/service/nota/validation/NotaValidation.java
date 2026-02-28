package br.com.poupacompra.integracao.service.nota.validation;

import org.springframework.stereotype.Component;

import br.com.poupacompra.integracao.common.exception.NotaJaCadastradaException;
import br.com.poupacompra.integracao.repository.NotaRepository;

@Component
public class NotaValidation {

  private final NotaRepository notaRepository;

  public NotaValidation(NotaRepository notaRepository) {
    this.notaRepository = notaRepository;
  }
  
  public void validarNotaExistente(String chaveAcesso) {
    var notaExistente = notaRepository.findByChaveAcesso(chaveAcesso);
    if (notaExistente.isPresent()) {
      throw new NotaJaCadastradaException("Nota já cadastrada no sistema.");
    }
  }

}
