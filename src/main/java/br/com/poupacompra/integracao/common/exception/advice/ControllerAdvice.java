package br.com.poupacompra.integracao.common.exception.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.poupacompra.integracao.common.exception.NotaJaCadastradaException;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(NotaJaCadastradaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleNotaJaCadastradaException(NotaJaCadastradaException ex) {
        return ex.getMessage();
    }
}
