package br.com.poupacompra.integracao.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.integracao.dto.listacompra.CategoriaProdutoResponse;
import br.com.poupacompra.integracao.dto.listacompra.PaginaResponse;
import br.com.poupacompra.integracao.dto.listacompra.ProdutoResponse;
import br.com.poupacompra.integracao.service.listacompra.CatalogoProdutoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Catálogo de produtos")
@SecurityRequirement(name = "bearerAuth")
public class CatalogoProdutoController {
    private final CatalogoProdutoService service;

    public CatalogoProdutoController(CatalogoProdutoService service) {
        this.service = service;
    }

    @GetMapping("/categorias")
    public List<CategoriaProdutoResponse> listarCategorias() {
        return service.listarCategorias();
    }

    @GetMapping("/produtos")
    public PaginaResponse<ProdutoResponse> pesquisarProdutos(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
        return service.pesquisarProdutos(busca, categoriaId, pagina, tamanho);
    }
}