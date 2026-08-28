package br.com.poupacompra.integracao.service.nota;

import java.util.List;

import br.com.poupacompra.integracao.dto.nota.NotaCompletaDTO;
import br.com.poupacompra.integracao.model.nota.GeralNota;
import br.com.poupacompra.integracao.model.nota.ItensNota;

public interface NotaService {

   GeralNota salvarNota(NotaCompletaDTO notaDTO);

   List<GeralNota> listarNotas();

   List<ItensNota> listarItensDaNota(Long notaId);
}
