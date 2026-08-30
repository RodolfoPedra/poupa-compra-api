package br.com.poupacompra.integracao.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.integracao.common.converter.impl.GeralNotaConverter;
import br.com.poupacompra.integracao.common.converter.impl.ItensNotaConverter;
import br.com.poupacompra.integracao.dto.nota.ItensNotaDTO;
import br.com.poupacompra.integracao.dto.nota.NotaCompletaDTO;
import br.com.poupacompra.integracao.dto.nota.NotaDTO;
import br.com.poupacompra.integracao.model.nota.GeralNota;
import br.com.poupacompra.integracao.service.nota.NotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/notas", produces = "application/json")
@Tag(name = "Nota Controller", description = "Endpoints para gerenciamento de notas fiscais")
@SecurityRequirement(name = "bearerAuth")
public class NotaController {

    private final NotaService notaService;
    private final GeralNotaConverter geralNotaConverter;
    private final ItensNotaConverter itensNotaConverter;

    public NotaController(@Autowired NotaService notaService, @Autowired GeralNotaConverter geralNotaConverter,
            @Autowired ItensNotaConverter itensNotaConverter) {
        this.notaService = notaService;
        this.geralNotaConverter = geralNotaConverter;
        this.itensNotaConverter = itensNotaConverter;
    }

    @Operation(summary = "Salvar Nota Fiscal", description = "Endpoint para salvar uma nova nota fiscal", method = "POST")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Nota salva com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
        public ResponseEntity<NotaDTO> salvarNota(@Valid @RequestBody NotaCompletaDTO nota) {

        GeralNota geralNota = notaService.salvarNota(nota);
        NotaDTO notaDTO = geralNotaConverter.entityToDto(geralNota);

        return ResponseEntity.status(HttpStatus.CREATED).body(notaDTO);
    }

    @Operation(summary = "Listar Notas Fiscais", description = "Endpoint para listar todas as notas fiscais", method = "GET")
    @GetMapping
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

    @Operation(summary = "Listar Itens da Nota", description = "Endpoint para listar os itens vinculados a uma nota fiscal", method = "GET")
    @GetMapping("/{id}/itens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Itens listados com sucesso"),
            @ApiResponse(responseCode = "403", description = "A nota pertence a outro usuário"),
            @ApiResponse(responseCode = "404", description = "Nota não encontrada")
    })
    public ResponseEntity<List<ItensNotaDTO>> listarItensDaNota(@PathVariable Long id) {
        List<ItensNotaDTO> itensDTO = notaService.listarItensDaNota(id).stream()
                .map(itensNotaConverter::entityToDto)
                .toList();
        return ResponseEntity.ok(itensDTO);
    }
}
