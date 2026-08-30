package br.com.poupacompra.integracao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.poupacompra.integracao.model.listacompra.CategoriaProduto;

public interface CategoriaProdutoRepository extends JpaRepository<CategoriaProduto, Long> {
    List<CategoriaProduto> findAllByAtivoTrueOrderByNomeAsc();
}