package br.com.poupacompra.integracao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.integracao.dto.listacompra.CompartilhamentoListaResponse;
import br.com.poupacompra.integracao.dto.listacompra.ConvidarUsuarioListaRequest;
import br.com.poupacompra.integracao.dto.listacompra.ListaCompartilhadaResumoResponse;
import br.com.poupacompra.integracao.dto.listacompra.ListaCompraResponse;
import br.com.poupacompra.integracao.service.listacompra.ListaCompraCompartilhamentoService;
import br.com.poupacompra.integracao.service.listacompra.ListaCompraService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/listas")
@Tag(name = "Compartilhamento de listas")
@SecurityRequirement(name = "bearerAuth")
public class ListaCompraCompartilhamentoController {
    private final ListaCompraCompartilhamentoService compartilhamentoService;
    private final ListaCompraService listaService;

    public ListaCompraCompartilhamentoController(ListaCompraCompartilhamentoService compartilhamentoService,
            ListaCompraService listaService) {
        this.compartilhamentoService = compartilhamentoService;
        this.listaService = listaService;
    }

    @PostMapping("/{listaId}/convite")
    public ResponseEntity<CompartilhamentoListaResponse> convidar(Authentication authentication,
            @PathVariable Long listaId, @Valid @RequestBody ConvidarUsuarioListaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compartilhamentoService
                .convidar(authentication.getName(), listaId, request.email(), request.updatedAt()));
    }

    @DeleteMapping("/{listaId}/convite")
    public ResponseEntity<Void> cancelar(Authentication authentication, @PathVariable Long listaId) {
        compartilhamentoService.cancelar(authentication.getName(), listaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/convites")
    public List<CompartilhamentoListaResponse> listarConvites(Authentication authentication) {
        return compartilhamentoService.listarConvites(authentication.getName());
    }

    @PostMapping("/convites/{conviteId}/aceite")
    public CompartilhamentoListaResponse aceitar(Authentication authentication, @PathVariable Long conviteId) {
        return compartilhamentoService.aceitar(authentication.getName(), conviteId);
    }

    @PostMapping("/convites/{conviteId}/recusa")
    public ResponseEntity<Void> recusar(Authentication authentication, @PathVariable Long conviteId) {
        compartilhamentoService.recusar(authentication.getName(), conviteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/compartilhadas")
    public List<ListaCompartilhadaResumoResponse> listarCompartilhadas(Authentication authentication) {
        return compartilhamentoService.listarCompartilhadas(authentication.getName());
    }

    @GetMapping("/compartilhadas/{listaId}")
    public ListaCompraResponse buscarCompartilhada(Authentication authentication, @PathVariable Long listaId) {
        return listaService.buscar(authentication.getName(), listaId);
    }

    @DeleteMapping("/compartilhadas/{listaId}/participacao")
    public ResponseEntity<Void> sair(Authentication authentication, @PathVariable Long listaId) {
        compartilhamentoService.sair(authentication.getName(), listaId);
        return ResponseEntity.noContent().build();
    }
}