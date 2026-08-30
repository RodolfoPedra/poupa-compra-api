package br.com.poupacompra.integracao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.poupacompra.integracao.model.usuario.AuthProvider;
import br.com.poupacompra.integracao.model.usuario.UsuarioProvider;

public interface UsuarioProviderRepository extends JpaRepository<UsuarioProvider, Long> {
    Optional<UsuarioProvider> findByProviderAndSubject(AuthProvider provider, String subject);
}