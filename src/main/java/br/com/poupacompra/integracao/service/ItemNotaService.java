package br.com.poupacompra.integracao.service;

import br.com.poupacompra.integracao.dto.NotaCompletaDTO;
import br.com.poupacompra.integracao.model.ItensNota;

public interface ItemNotaService {

  ItensNota salvarItensNota(NotaCompletaDTO notaDTO);
}
