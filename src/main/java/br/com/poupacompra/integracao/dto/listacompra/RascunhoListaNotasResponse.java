package br.com.poupacompra.integracao.dto.listacompra;

import java.util.List;

public record RascunhoListaNotasResponse(String nome, List<ItemRascunhoListaResponse> itens) {
}