package br.com.poupacompra.integracao.common.exception.advice;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ControllerAdvice {

    @ExceptionHandler(NotaJaCadastradaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleNotaJaCadastradaException(NotaJaCadastradaException ex, HttpServletRequest request) {
        log.warn("businessError status=400 type={} path={} message={}", ex.getClass().getSimpleName(), request.getRequestURI(), ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(NotaNaoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String handleNotaNaoEncontradaException(NotaNaoEncontradaException ex, HttpServletRequest request) {
        log.warn("businessError status=404 type={} path={} message={}", ex.getClass().getSimpleName(), request.getRequestURI(), ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String handleRecursoNaoEncontradoException(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        log.warn("businessError status=404 type={} path={} message={}", ex.getClass().getSimpleName(), request.getRequestURI(), ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleRegraNegocioException(RegraNegocioException ex, HttpServletRequest request) {
        log.warn("businessError status=400 type={} path={} message={}", ex.getClass().getSimpleName(), request.getRequestURI(), ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(ConflitoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String handleConflitoException(ConflitoException ex, HttpServletRequest request) {
        log.warn("businessError status=409 type={} path={} message={}", ex.getClass().getSimpleName(), request.getRequestURI(), ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("businessError status=403 type={} path={}", ex.getClass().getSimpleName(), request.getRequestURI());
        return "Acesso negado";
    }

    @ExceptionHandler({EmailJaCadastradoException.class, TokenInvalidoException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleAuthException(RuntimeException ex, HttpServletRequest request) {
        log.warn("businessError status=400 type={} path={} message={}", ex.getClass().getSimpleName(), request.getRequestURI(), ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    void handleNoResourceFoundException() {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField()).distinct().collect(Collectors.joining(","));
        log.warn("validationError status=400 path={} fields={}", request.getRequestURI(), campos);
        return "Dados inválidos";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("unexpectedError path={} type={}", request.getRequestURI(), ex.getClass().getSimpleName(), ex);
        return "Erro interno. Informe o identificador da requisição ao suporte.";
    }
}
