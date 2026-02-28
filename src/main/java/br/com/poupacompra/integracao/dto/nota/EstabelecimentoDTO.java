package br.com.poupacompra.integracao.dto.nota;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstabelecimentoDTO {

    private String nomeEstabelecimento;
    private String cpfCnpj;
    private String endereco;
}
