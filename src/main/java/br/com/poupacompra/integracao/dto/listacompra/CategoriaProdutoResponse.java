package br.com.poupacompra.integracao.dto.listacompra;

import br.com.poupacompra.integracao.model.listacompra.CategoriaProduto;

public record CategoriaProdutoResponse(Long id, String nome) {
    public static CategoriaProdutoResponse from(CategoriaProduto categoria) {
        return new CategoriaProdutoResponse(categoria.getId(), categoria.getNome());
    }
}