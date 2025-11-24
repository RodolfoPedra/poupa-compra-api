package br.com.poupacompra.integracao.model.nota;

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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
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

    @Size(max = 5)
    @Column(name = "quantidade", length = 5)
    private String quantidade;

    @Size(max = 3)
    @Column(name = "tipo_unidade", length = 3)
    private String tipoUnidade;

    @NotNull
    @Column(name = "valor_unitario", nullable = false)
    private Float valorUnitario;

    @NotNull
    @Column(name = "valortotal", nullable = false)
    private Float valortotal;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_id", referencedColumnName = "id", nullable = false)
    private GeralNota nota;
}