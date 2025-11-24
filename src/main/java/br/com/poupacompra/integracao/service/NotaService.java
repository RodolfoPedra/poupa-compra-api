package br.com.poupacompra.integracao.service;

import br.com.poupacompra.integracao.dto.NotaCompletaDTO;
import br.com.poupacompra.integracao.model.nota.GeralNota;

public interface NotaService {

   GeralNota salvarNota(NotaCompletaDTO notaDTO);
}
