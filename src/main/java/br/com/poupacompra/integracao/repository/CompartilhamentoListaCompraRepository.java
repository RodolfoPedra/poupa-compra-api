package br.com.poupacompra.integracao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import br.com.poupacompra.integracao.model.listacompra.CompartilhamentoListaCompra;
import br.com.poupacompra.integracao.model.listacompra.StatusCompartilhamentoLista;
import jakarta.persistence.LockModeType;

public interface CompartilhamentoListaCompraRepository extends JpaRepository<CompartilhamentoListaCompra, Long> {
    boolean existsByListaId(Long listaId);

        @EntityGraph(attributePaths = { "lista", "lista.usuario", "convidado" })
        Optional<CompartilhamentoListaCompra> findByListaId(Long listaId);

        Optional<CompartilhamentoListaCompra> findByListaIdAndConvidadoIdAndStatus(
            Long listaId, Long convidadoId, StatusCompartilhamentoLista status);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = { "lista", "lista.usuario", "convidado" })
    Optional<CompartilhamentoListaCompra> findByIdAndConvidadoId(Long id, Long convidadoId);

    @EntityGraph(attributePaths = { "lista", "lista.usuario", "convidado" })
    List<CompartilhamentoListaCompra> findAllByConvidadoIdAndStatusOrderByCreatedAtDesc(
            Long convidadoId, StatusCompartilhamentoLista status);
}