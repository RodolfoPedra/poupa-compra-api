package br.com.poupacompra.integracao.dto.nota;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotaCompletaDTO {

  private EstabelecimentoDTO estabelecimento;
  private List<ItensNotaDTO> itensNota;
  private NotaDTO nota;
}
