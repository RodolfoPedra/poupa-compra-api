package br.com.poupacompra.integracao.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.integracao.dto.NotaCompletaDTO;
import br.com.poupacompra.integracao.model.nota.GeralNota;
import br.com.poupacompra.integracao.service.NotaService;


@RestController
public class NotaController {

  @Autowired
  private NotaService notaService;

    @PostMapping
    @RequestMapping("/salvar-nota")
    public ResponseEntity<GeralNota> create(@RequestBody NotaCompletaDTO Nota){
        GeralNota completaDTO = notaService.salvarNota(Nota);
        return null;
    }
}
