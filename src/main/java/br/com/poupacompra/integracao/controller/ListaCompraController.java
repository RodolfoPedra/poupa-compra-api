package br.com.poupacompra.integracao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.integracao.dto.listacompra.EstabelecimentoNotaResponse;
import br.com.poupacompra.integracao.dto.listacompra.GerarRascunhoListaRequest;
import br.com.poupacompra.integracao.dto.listacompra.ListaCompraResponse;
import br.com.poupacompra.integracao.dto.listacompra.ListaCompraResumoResponse;
import br.com.poupacompra.integracao.dto.listacompra.NotaOrigemListaResponse;
import br.com.poupacompra.integracao.dto.listacompra.PaginaResponse;
import br.com.poupacompra.integracao.dto.listacompra.RascunhoListaNotasResponse;
import br.com.poupacompra.integracao.dto.listacompra.SalvarListaCompraRequest;
import br.com.poupacompra.integracao.service.listacompra.ListaCompraOrigemNotaService;
import br.com.poupacompra.integracao.service.listacompra.ListaCompraService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/v1/listas")
@Tag(name = "Listas de compras")
@SecurityRequirement(name = "bearerAuth")
public class ListaCompraController {
    private final ListaCompraService service;
    private final ListaCompraOrigemNotaService origemNotaService;

    public ListaCompraController(ListaCompraService service, ListaCompraOrigemNotaService origemNotaService) {
        this.service = service;
        this.origemNotaService = origemNotaService;
    }

    @PostMapping
    public ResponseEntity<ListaCompraResponse> criar(Authentication authentication,
            @Valid @RequestBody SalvarListaCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(authentication.getName(), request));
    }

    @GetMapping
    public List<ListaCompraResumoResponse> listar(Authentication authentication) {
        return service.listar(authentication.getName());
    }

    @GetMapping("/{listaId}")
    public ListaCompraResponse buscar(Authentication authentication, @PathVariable Long listaId) {
        return service.buscar(authentication.getName(), listaId);
    }

    @PutMapping("/{listaId}")
    public ListaCompraResponse salvar(Authentication authentication, @PathVariable Long listaId,
            @Valid @RequestBody SalvarListaCompraRequest request) {
        return service.salvar(authentication.getName(), listaId, request);
    }

    @DeleteMapping("/{listaId}")
    public ResponseEntity<Void> excluir(Authentication authentication, @PathVariable Long listaId) {
        service.excluir(authentication.getName(), listaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/origem-notas/estabelecimentos")
    public List<EstabelecimentoNotaResponse> listarEstabelecimentos(Authentication authentication) {
        return origemNotaService.listarEstabelecimentos(authentication.getName());
    }

    @GetMapping("/origem-notas/estabelecimentos/{estabelecimentoId}/notas")
    public PaginaResponse<NotaOrigemListaResponse> listarNotas(Authentication authentication,
            @PathVariable Long estabelecimentoId,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamanho) {
        return origemNotaService.listarNotas(authentication.getName(), estabelecimentoId, pagina, tamanho);
    }

    @PostMapping("/origem-notas/rascunho")
    public RascunhoListaNotasResponse gerarRascunho(Authentication authentication,
            @Valid @RequestBody GerarRascunhoListaRequest request) {
        return origemNotaService.gerarRascunho(authentication.getName(), request.notaIds());
    }

}