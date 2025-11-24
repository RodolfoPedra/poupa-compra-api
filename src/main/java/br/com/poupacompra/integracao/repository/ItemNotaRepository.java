package br.com.poupacompra.integracao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.poupacompra.integracao.model.nota.ItensNota;

public interface ItemNotaRepository extends JpaRepository<ItensNota, Long>{

}
