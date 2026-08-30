package br.com.poupacompra.integracao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.poupacompra.integracao.model.listacompra.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    java.util.Optional<Produto> findByIdAndAtivoTrue(Long id);

    @EntityGraph(attributePaths = "categoria")
    Page<Produto> findAllByAtivoTrueAndCategoriaAtivoTrue(Pageable pageable);

    @EntityGraph(attributePaths = "categoria")
    Page<Produto> findAllByAtivoTrueAndCategoriaAtivoTrueAndCategoriaId(Long categoriaId, Pageable pageable);

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            SELECT produto FROM Produto produto
            WHERE produto.ativo = true
              AND produto.categoria.ativo = true
        AND (LOWER(produto.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
       OR LOWER(produto.categoria.nome) LIKE LOWER(CONCAT('%', :busca, '%')))
            """)
    Page<Produto> pesquisar(@Param("busca") String busca, Pageable pageable);

    @EntityGraph(attributePaths = "categoria")
    @Query("""
      SELECT produto FROM Produto produto
      WHERE produto.ativo = true
        AND produto.categoria.ativo = true
        AND produto.categoria.id = :categoriaId
        AND (LOWER(produto.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
       OR LOWER(produto.categoria.nome) LIKE LOWER(CONCAT('%', :busca, '%')))
      """)
    Page<Produto> pesquisarPorCategoria(@Param("busca") String busca, @Param("categoriaId") Long categoriaId,
      Pageable pageable);
}