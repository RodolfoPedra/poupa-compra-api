package br.com.poupacompra.integracao.dto.listacompra;

import br.com.poupacompra.integracao.model.listacompra.Produto;

public record ProdutoResponse(Long id, String nome, Long categoriaId, String categoriaNome) {
    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(produto.getId(), produto.getNome(), produto.getCategoria().getId(),
                produto.getCategoria().getNome());
    }
}