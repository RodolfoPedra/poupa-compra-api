package br.com.poupacompra.integracao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.poupacompra.integracao.model.nota.GeralNota;

public interface NotaRepository extends JpaRepository<GeralNota, Long>{

}
