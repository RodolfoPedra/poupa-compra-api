package br.com.poupacompra.integracao.service.listacompra;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.poupacompra.integracao.dto.listacompra.CategoriaProdutoResponse;
import br.com.poupacompra.integracao.dto.listacompra.PaginaResponse;
import br.com.poupacompra.integracao.dto.listacompra.ProdutoResponse;
import br.com.poupacompra.integracao.model.listacompra.Produto;
import br.com.poupacompra.integracao.repository.CategoriaProdutoRepository;
import br.com.poupacompra.integracao.repository.ProdutoRepository;

@Service
public class CatalogoProdutoService {
    private final CategoriaProdutoRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public CatalogoProdutoService(CategoriaProdutoRepository categoriaRepository, ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaProdutoResponse> listarCategorias() {
        return categoriaRepository.findAllByAtivoTrueOrderByNomeAsc().stream()
                .map(CategoriaProdutoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ProdutoResponse> pesquisarProdutos(String busca, Long categoriaId, int pagina, int tamanho) {
        String termo = busca == null || busca.isBlank() ? null : busca.trim();
        var pageable = PageRequest.of(pagina, tamanho, Sort.by("nome").ascending());
        Page<Produto> produtos;
        if (termo == null && categoriaId == null) {
            produtos = produtoRepository.findAllByAtivoTrueAndCategoriaAtivoTrue(pageable);
        } else if (termo == null) {
            produtos = produtoRepository.findAllByAtivoTrueAndCategoriaAtivoTrueAndCategoriaId(categoriaId, pageable);
        } else if (categoriaId == null) {
            produtos = produtoRepository.pesquisar(termo, pageable);
        } else {
            produtos = produtoRepository.pesquisarPorCategoria(termo, categoriaId, pageable);
        }
        return PaginaResponse.from(produtos.map(ProdutoResponse::from));
    }
}