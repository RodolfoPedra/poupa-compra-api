package br.com.poupacompra.integracao.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotaCompletaDTO {

  private EstabelecimentoDTO estabelecimento;
  private List<ItensNotaDTO> itensNota;
  private NotaDTO nota;
}
