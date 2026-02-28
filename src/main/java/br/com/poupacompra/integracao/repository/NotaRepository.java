package br.com.poupacompra.integracao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.poupacompra.integracao.model.nota.GeralNota;

public interface NotaRepository extends JpaRepository<GeralNota, Long>{

  Optional<GeralNota> findByChaveAcesso(String chaveAcesso);

}
