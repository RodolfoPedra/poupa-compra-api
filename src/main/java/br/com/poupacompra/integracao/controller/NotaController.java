package br.com.poupacompra.integracao.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.integracao.dto.nota.NotaCompletaDTO;
import br.com.poupacompra.integracao.service.NotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(produces = "application/json")
@Tag(name = "Nota Controller", description = "Endpoints para gerenciamento de notas fiscais")
public class NotaController {

    private final NotaService notaService;

    public NotaController(@Autowired NotaService notaService) {
        this.notaService = notaService;
    }

    @Operation(summary = "Salvar Nota Fiscal", description = "Endpoint para salvar uma nova nota fiscal", method = "POST")
    @PostMapping(value = "/salvar-nota", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota salva com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Void> salvarNota(@RequestBody NotaCompletaDTO nota) {
        notaService.salvarNota(nota);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
