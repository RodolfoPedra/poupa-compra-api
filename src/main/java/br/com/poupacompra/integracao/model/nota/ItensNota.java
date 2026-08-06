package br.com.poupacompra.integracao.model.nota;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "itens_nota")
public class ItensNota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "descricao", nullable = false, length = 50)
    private String descricao;

    @NotNull
    @Column(name = "quantidade", nullable = false)
    private Float quantidade;

    @Size(max = 5)
    @Column(name = "tipo_unidade", length = 5)
    private String tipoUnidade;

    @NotNull
    @Column(name = "valor_unitario", nullable = false)
    private Float valorUnitario;

    @NotNull
    @Column(name = "codigo_item", nullable = false)
    private Long codigoItem;

    @NotNull
    @Column(name = "valor_total", nullable = false)
    private Float valorTotal;

    @NotNull
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_id", referencedColumnName = "id", nullable = false)
    private GeralNota nota;
}