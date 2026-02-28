package br.com.poupacompra.integracao.dto.nota;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItensNotaDTO {
     
     private String descricao;
     private Float quantidade;
     private String tipoUnidade;
     private Float valorUnitario;
     private Float valorTotal;
}
