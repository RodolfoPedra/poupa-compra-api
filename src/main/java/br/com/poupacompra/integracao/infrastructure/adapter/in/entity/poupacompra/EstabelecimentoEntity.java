package br.com.poupacompra.integracao.infrastructure.adapter.in.entity.poupacompra;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
// @Entity
// @Table(name = "estabelecimento", schema = "notas")
public class EstabelecimentoEntity {

  // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "estabelecimento_sequence")
  // @SequenceGenerator(name = "estabelecimento_sequence", sequenceName = "nota_id_seq")
  // @Column(name = "id", nullable = false)
  @Id
  private Long id;
  
  // @Column(name = "nome")
  private String nome;

  // @Column(name = "cnpj")
  private String cnpj;

  // @Column(name = "endereco")
  private String endereco;

  // @Column(name = "nota_id")
  private Long notaId;
}
