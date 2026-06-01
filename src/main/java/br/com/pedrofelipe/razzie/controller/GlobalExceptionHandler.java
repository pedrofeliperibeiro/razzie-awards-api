package br.com.pedrofelipe.razzie.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Padroniza todas as respostas de erro no formato ProblemDetail (RFC 7807),
 * com {@code Content-Type: application/problem+json}.
 *
 * <p>Estendo {@link ResponseEntityExceptionHandler}: é ele que converte os erros
 * conhecidos do Spring MVC (404, 405, 415 etc.) em ProblemDetail. Sem estender, a
 * flag {@code spring.mvc.problemdetails.enabled} sozinha não cobre os erros do
 * dispatcher — eles cairiam no {@code BasicErrorController} e voltariam no formato
 * legado ({@code timestamp/status/error/path}). Com a herança, o corpo sai como
 * {@code application/problem+json} de verdade.
 *
 * <p>O {@link #handleUnexpected} abaixo é a rede de segurança para o inesperado: em
 * vez de vazar stack trace, devolvo um 500 enxuto e mando o detalhe pro log.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleUnexpected(Exception exception, WebRequest request) {
        log.error("Erro inesperado ao processar a requisição", exception);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado ao processar a requisição.");
        problem.setTitle("Internal Server Error");

        return handleExceptionInternal(
                exception, problem, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
