package br.com.poupacompra.integracao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.poupacompra.integracao.model.listacompra.ItemListaCompra;

public interface ItemListaCompraRepository extends JpaRepository<ItemListaCompra, Long> {
    @EntityGraph(attributePaths = "produto")
    List<ItemListaCompra> findAllByListaIdOrderByOrdemAscIdAsc(Long listaId);

    @EntityGraph(attributePaths = "produto")
    List<ItemListaCompra> findAllByListaId(Long listaId);
}