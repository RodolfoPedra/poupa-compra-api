package br.com.poupacompra.integracao.common.exception.advice;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.poupacompra.integracao.common.exception.ConflitoException;
import br.com.poupacompra.integracao.common.exception.EmailJaCadastradoException;
import br.com.poupacompra.integracao.common.exception.NotaJaCadastradaException;
import br.com.poupacompra.integracao.common.exception.NotaNaoEncontradaException;
import br.com.poupacompra.integracao.common.exception.RecursoNaoEncontradoException;
import br.com.poupacompra.integracao.common.exception.RegraNegocioException;
import br.com.poupacompra.integracao.common.exception.TokenInvalidoException;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(NotaJaCadastradaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleNotaJaCadastradaException(NotaJaCadastradaException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NotaNaoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String handleNotaNaoEncontradaException(NotaNaoEncontradaException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String handleRecursoNaoEncontradoException(RecursoNaoEncontradoException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleRegraNegocioException(RegraNegocioException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ConflitoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String handleConflitoException(ConflitoException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String handleAccessDeniedException(AccessDeniedException ex) {
        return "Acesso negado";
    }

    @ExceptionHandler({EmailJaCadastradoException.class, TokenInvalidoException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleAuthException(RuntimeException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    void handleNoResourceFoundException() {}
}
