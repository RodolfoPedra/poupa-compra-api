package br.com.poupacompra.integracao.service.nota.validation;

import org.springframework.stereotype.Component;

import br.com.poupacompra.integracao.common.exception.NotaJaCadastradaException;
import br.com.poupacompra.integracao.repository.NotaRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotaValidation {

  private final NotaRepository notaRepository;

  public NotaValidation(NotaRepository notaRepository) {
    this.notaRepository = notaRepository;
  }
  
  public void validarNotaExistente(String chaveAcesso) {
    var notaExistente = notaRepository.findByChaveAcesso(chaveAcesso);
    if (notaExistente.isPresent()) {
      log.warn("Nota já cadastrada no sistema. Chave de acesso: {}", chaveAcesso);
      throw new NotaJaCadastradaException("Nota já cadastrada no sistema.");
    }
  }

}
