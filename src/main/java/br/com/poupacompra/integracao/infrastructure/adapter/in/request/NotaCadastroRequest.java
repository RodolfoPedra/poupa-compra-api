package br.com.poupacompra.integracao.infrastructure.adapter.in.request;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class NotaCadastroRequest {
  private Estabelecimento estabelecimento;
  private List<Produto> produtos;
  private TotalProdutos totalProdutos;
}

@Getter
@Setter
class Estabelecimento {
  private String cnpj; 
  private String endereco; 
  private String nomeEstabelecimento; 
}

@Getter
@Setter
class Produto {
  private String produtoNome;
  private String produtoPreco;
  private String produtoQuantidade;
}

@Getter
@Setter
class TotalProdutos {
  	private String qtdItens;
		private String valorTotal;
}