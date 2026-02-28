package br.com.poupacompra.integracao.service.nota;

import br.com.poupacompra.integracao.dto.nota.NotaCompletaDTO;
import br.com.poupacompra.integracao.model.nota.ItensNota;

public interface ItemNotaService {

  ItensNota salvarItensNota(NotaCompletaDTO notaDTO);
}
