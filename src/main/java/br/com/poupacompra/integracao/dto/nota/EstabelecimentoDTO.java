package br.com.poupacompra.integracao.dto.nota;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstabelecimentoDTO {

    private String nomeEstabelecimento;
    private String cpfCnpj;
    private String endereco;
}
