package br.com.poupacompra.integracao.dto.listacompra;

import java.time.Instant;

import br.com.poupacompra.integracao.model.listacompra.CompartilhamentoListaCompra;
import br.com.poupacompra.integracao.model.listacompra.StatusCompartilhamentoLista;

public record CompartilhamentoListaResponse(Long id, Long listaId, String listaNome,
    StatusCompartilhamentoLista status,
        UsuarioCompartilhamentoResponse owner, UsuarioCompartilhamentoResponse convidado,
        Instant createdAt, Instant respondedAt) {
    public static CompartilhamentoListaResponse from(CompartilhamentoListaCompra compartilhamento) {
        if (compartilhamento == null) {
            return null;
        }
        return new CompartilhamentoListaResponse(compartilhamento.getId(), compartilhamento.getLista().getId(),
            compartilhamento.getLista().getNome(), compartilhamento.getStatus(),
                UsuarioCompartilhamentoResponse.from(compartilhamento.getLista().getUsuario()),
                UsuarioCompartilhamentoResponse.from(compartilhamento.getConvidado()),
                compartilhamento.getCreatedAt(), compartilhamento.getRespondedAt());
    }
}