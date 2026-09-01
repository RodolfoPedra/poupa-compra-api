package br.com.poupacompra.integracao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.poupacompra.integracao.model.listacompra.ItemListaCompra;
import jakarta.persistence.LockModeType;

public interface ItemListaCompraRepository extends JpaRepository<ItemListaCompra, Long> {
    @EntityGraph(attributePaths = "produto")
    List<ItemListaCompra> findAllByListaIdOrderByOrdemAscIdAsc(Long listaId);

    @EntityGraph(attributePaths = "produto")
    List<ItemListaCompra> findAllByListaId(Long listaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "produto")
    java.util.Optional<ItemListaCompra> findByIdAndListaId(Long id, Long listaId);

    boolean existsByListaIdAndProdutoId(Long listaId, Long produtoId);

    @Query("SELECT COALESCE(MAX(item.ordem), -1) FROM ItemListaCompra item WHERE item.lista.id = :listaId")
    int findMaiorOrdem(@Param("listaId") Long listaId);
}