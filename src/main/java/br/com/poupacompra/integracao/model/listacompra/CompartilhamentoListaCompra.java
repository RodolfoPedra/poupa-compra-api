package br.com.poupacompra.integracao.model.listacompra;

import java.time.Instant;

import br.com.poupacompra.integracao.model.usuario.Usuario;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "lista_compra_compartilhamento",
        uniqueConstraints = @UniqueConstraint(name = "uk_lista_compartilhamento_lista", columnNames = "lista_compra_id"))
public class CompartilhamentoListaCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lista_compra_id", nullable = false)
    private ListaCompra lista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "convidado_id", nullable = false)
    private Usuario convidado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCompartilhamentoLista status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public CompartilhamentoListaCompra(ListaCompra lista, Usuario convidado) {
        this.lista = lista;
        this.convidado = convidado;
        this.status = StatusCompartilhamentoLista.PENDENTE;
    }

    public void aceitar() {
        status = StatusCompartilhamentoLista.ACEITO;
        respondedAt = Instant.now();
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}