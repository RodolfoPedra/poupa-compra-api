package br.com.poupacompra.integracao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.poupacompra.integracao.model.listacompra.ListaCompra;
import jakarta.persistence.LockModeType;

public interface ListaCompraRepository extends JpaRepository<ListaCompra, Long> {
    @Query("""
            SELECT lista.id AS id, lista.nome AS nome,
                   COUNT(item.id) AS quantidadeItens,
                   COALESCE(SUM(CASE WHEN item.selecionado = true THEN 1 ELSE 0 END), 0) AS quantidadeSelecionados,
                   lista.createdAt AS createdAt, lista.updatedAt AS updatedAt
            FROM ListaCompra lista
            LEFT JOIN ItemListaCompra item ON item.lista = lista
            WHERE lista.usuario.id = :usuarioId
            GROUP BY lista.id, lista.nome, lista.createdAt, lista.updatedAt
            ORDER BY lista.updatedAt DESC
            """)
    List<ListaCompraResumoProjection> listarResumos(@Param("usuarioId") Long usuarioId);

    Optional<ListaCompra> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT lista FROM ListaCompra lista WHERE lista.id = :id AND lista.usuario.id = :usuarioId")
    Optional<ListaCompra> findByIdAndUsuarioIdForUpdate(@Param("id") Long id, @Param("usuarioId") Long usuarioId);
    boolean existsByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);
    boolean existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(Long usuarioId, String nome, Long id);
}