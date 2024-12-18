package br.com.poupacompra.integracao.infrastructure.adapter.in.entity.poupacompra;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
// @Entity
// @Table(name = "produto", schema = "notas")
public class ProdutoEntity {

  // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "produto_sequence")
  // @SequenceGenerator(name = "produto_sequence", sequenceName = "nota_id_seq")
  // @Column(name = "id")
  @Id
  private Long id;
  
  // @Column(name = "nome")
  private String nome;

  // @Column(name = "valor_unitario")
  private float valorUnitario;

  // @Column(name = "quantidade")
  private Integer quantidade;

  // @Column(name = "tipo_unidade")
  private String tipoUnidade;

  // @Column(name = "valor_total")
  private float valorTotal;

  // @Column(name = "nota_id")
  private Long notaId;
}
