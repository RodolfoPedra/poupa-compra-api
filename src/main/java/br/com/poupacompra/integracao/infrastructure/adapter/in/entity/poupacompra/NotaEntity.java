package br.com.poupacompra.integracao.infrastructure.adapter.in.entity.poupacompra;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
// @Entity
// @Table(name = "nota", schema = "notas")
public class NotaEntity {

  // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nota_sequence")
  // @SequenceGenerator(name = "nota_sequence", sequenceName = "nota_id_seq")
  // @Column(name = "id")
  @Id
  private Long id; 

  // @Column(name = "uf")
  private String uf;

  // @Column(name = "chave_acesso")
  private String chaveAcesso;

  // @Column(name = "numero_nota")
  private Integer numeroNota;

  // @Column(name = "nome_consumidor")
  private String nomeConsumidor;

  // @Column(name = "cpf_cnpj_consumidor")
  private String cpfCnpjConsumidor;

  // @Column(name = "valor_total")
  private float valorTotal;

  // @Column(name = "total_itens")
  private Integer totalItens;
}

