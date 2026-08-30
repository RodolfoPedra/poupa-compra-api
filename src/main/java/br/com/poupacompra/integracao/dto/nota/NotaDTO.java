package br.com.poupacompra.integracao.dto.nota;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotaDTO {

    private Long id;
    private Integer quantidadeItens;
    private float valorTotal;
    private Long usuario;
    private Integer numeroCfe;
    private String ufCfe;
    private String dataHoraEmissao;
    private String urlCfe;
    private String chaveAcesso;
}
