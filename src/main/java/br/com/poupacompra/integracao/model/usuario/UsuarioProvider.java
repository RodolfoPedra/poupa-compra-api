package br.com.poupacompra.integracao.model.usuario;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuario_provider", uniqueConstraints = @UniqueConstraint(name = "uk_provider_subject", columnNames = { "provider", "subject" }))
public class UsuarioProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(name = "provider_email", length = 320)
    private String providerEmail;

    public UsuarioProvider(Usuario usuario, AuthProvider provider, String subject, String providerEmail) {
        this.usuario = usuario;
        this.provider = provider;
        this.subject = subject;
        this.providerEmail = providerEmail;
    }
}