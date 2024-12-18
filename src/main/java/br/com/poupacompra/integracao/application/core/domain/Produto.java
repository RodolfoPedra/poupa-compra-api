package br.com.poupacompra.integracao.application.core.domain;

public class Produto {

  private Long id;
  
  private String nome;

  private float valorUnitario;

  private Integer quantidade;

  private String tipoUnidade;

  private float valorTotal;

  private Long notaId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public float getValorUnitario() {
    return valorUnitario;
  }

  public void setValorUnitario(float valorUnitario) {
    this.valorUnitario = valorUnitario;
  }

  public Integer getQuantidade() {
    return quantidade;
  }

  public void setQuantidade(Integer quantidade) {
    this.quantidade = quantidade;
  }

  public String getTipoUnidade() {
    return tipoUnidade;
  }

  public void setTipoUnidade(String tipoUnidade) {
    this.tipoUnidade = tipoUnidade;
  }

  public float getValorTotal() {
    return valorTotal;
  }

  public void setValorTotal(float valorTotal) {
    this.valorTotal = valorTotal;
  }

  public Long getNotaId() {
    return notaId;
  }

  public void setNotaId(Long notaId) {
    this.notaId = notaId;
  }

  
}
