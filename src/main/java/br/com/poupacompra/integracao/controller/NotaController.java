package br.com.poupacompra.integracao.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.integracao.common.converter.impl.GeralNotaConverter;
import br.com.poupacompra.integracao.dto.nota.NotaCompletaDTO;
import br.com.poupacompra.integracao.dto.nota.NotaDTO;
import br.com.poupacompra.integracao.model.nota.GeralNota;
import br.com.poupacompra.integracao.service.nota.NotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(produces = "application/json")
@Tag(name = "Nota Controller", description = "Endpoints para gerenciamento de notas fiscais")
public class NotaController {

    private final NotaService notaService;
    private final GeralNotaConverter geralNotaConverter;

    public NotaController(@Autowired NotaService notaService, @Autowired GeralNotaConverter geralNotaConverter) {
        this.notaService = notaService;
        this.geralNotaConverter = geralNotaConverter;
    }

    @Operation(summary = "Salvar Nota Fiscal", description = "Endpoint para salvar uma nova nota fiscal", method = "POST")
    @PostMapping(value = "/salvar-nota", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota salva com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<NotaDTO> salvarNota(@RequestBody NotaCompletaDTO nota) {

        GeralNota geralNota = notaService.salvarNota(nota);
        NotaDTO notaDTO = geralNotaConverter.entityToDto(geralNota);

        return ResponseEntity.status(HttpStatus.CREATED).body(notaDTO);
    }

    @Operation(summary = "Listar Notas Fiscais", description = "Endpoint para listar todas as notas fiscais", method = "GET")
    @GetMapping(value = "/listar-notas", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notas listadas com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<NotaDTO>> listarNotas() {
        List<GeralNota> notas = notaService.listarNotas();
        List<NotaDTO> notasDTO = notas.stream()
                .map(geralNotaConverter::entityToDto)
                .toList();
        return ResponseEntity.ok(notasDTO);
    }
}
