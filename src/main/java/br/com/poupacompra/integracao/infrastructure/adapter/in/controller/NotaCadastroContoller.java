package br.com.poupacompra.integracao.infrastructure.adapter.in.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.integracao.infrastructure.adapter.in.request.NotaCadastroRequest;

@RestController
@RequestMapping("/nota-cadastro")
public class NotaCadastroContoller {

  @PostMapping
  public void cadastrarNota(@RequestBody NotaCadastroRequest request) {
    System.out.println(request);
  }
}
