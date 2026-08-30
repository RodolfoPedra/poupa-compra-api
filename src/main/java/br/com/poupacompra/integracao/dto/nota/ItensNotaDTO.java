package br.com.poupacompra.integracao.dto.nota;

import jakarta.validation.constraints.NotNull;
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
     @NotNull(message = "Código do item é obrigatório")
     private Long codigoItem;
     private Float valorUnitario;
     private Float valorTotal;
}
