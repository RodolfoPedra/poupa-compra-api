package br.com.poupacompra.integracao.dto.listacompra;

import java.time.Instant;
import java.util.List;

import br.com.poupacompra.integracao.model.listacompra.ListaCompra;

public record ListaCompraResponse(Long id, String nome, Instant createdAt, Instant updatedAt,
    NotaVinculadaResponse nota, PerfilAcessoLista perfilAcesso, boolean modoColaborativo,
    CompartilhamentoListaResponse compartilhamento,
        List<ItemListaCompraResponse> itens) {
    public static ListaCompraResponse from(ListaCompra lista, List<ItemListaCompraResponse> itens) {
        return new ListaCompraResponse(lista.getId(), lista.getNome(), lista.getCreatedAt(), lista.getUpdatedAt(),
        NotaVinculadaResponse.from(lista.getNota()), PerfilAcessoLista.OWNER, false, null, itens);
    }

    public static ListaCompraResponse from(ListaCompra lista, List<ItemListaCompraResponse> itens,
        PerfilAcessoLista perfilAcesso, CompartilhamentoListaResponse compartilhamento) {
    return new ListaCompraResponse(lista.getId(), lista.getNome(), lista.getCreatedAt(), lista.getUpdatedAt(),
        NotaVinculadaResponse.from(lista.getNota()), perfilAcesso, compartilhamento != null,
        compartilhamento, itens);
    }
}