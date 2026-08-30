package br.com.poupacompra.integracao.model.listacompra;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import br.com.poupacompra.integracao.model.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "lista_compra")
public class ListaCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Setter
    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ListaCompra(Usuario usuario, String nome) {
        this.usuario = usuario;
        this.nome = nome;
    }

    public void touch() {
        Instant agora = Instant.now();
        Instant proximoMilissegundo = updatedAt == null
            ? agora
            : updatedAt.truncatedTo(ChronoUnit.MILLIS).plusMillis(1);
        updatedAt = agora.isAfter(proximoMilissegundo) ? agora : proximoMilissegundo;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        touch();
    }
}