package br.com.poupacompra.integracao.dto.listacompra;

import java.time.Instant;

public record EventoListaResponse(TipoEventoLista tipo, Long listaId, Instant listaUpdatedAt,
        String nome, ItemListaCompraResponse item, Long itemId) {
}