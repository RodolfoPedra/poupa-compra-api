package br.com.poupacompra.integracao.service.listacompra;

import br.com.poupacompra.integracao.dto.listacompra.PerfilAcessoLista;
import br.com.poupacompra.integracao.model.listacompra.CompartilhamentoListaCompra;
import br.com.poupacompra.integracao.model.listacompra.ListaCompra;

public record AcessoListaCompra(ListaCompra lista, PerfilAcessoLista perfil,
        CompartilhamentoListaCompra compartilhamento) {
}