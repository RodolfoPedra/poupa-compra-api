package br.com.poupacompra.integracao.model.listacompra;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "item_lista_compra")
public class ItemListaCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lista_compra_id", nullable = false)
    private ListaCompra lista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    @Setter
    private Produto produto;

    @Setter
    @Column(nullable = false, length = 160)
    private String descricao;

    @Setter
    @Column(precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UnidadeMedida unidade;

    @Setter
    @Column(nullable = false)
    private boolean selecionado;

    @Column(nullable = false)
    @Setter
    private int ordem;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ItemListaCompra(ListaCompra lista, Produto produto, String descricao, BigDecimal quantidade,
            UnidadeMedida unidade, int ordem) {
        this.lista = lista;
        this.produto = produto;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.ordem = ordem;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}