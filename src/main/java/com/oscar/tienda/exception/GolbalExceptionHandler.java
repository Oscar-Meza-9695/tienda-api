package com.oscar.tienda.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GolbalExceptionHandler {
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> noEncontrado(RecursoNoEncontradoException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> credenciales(AuthenticationException e) {
        // Mismo mensaje si falla el usuario o la contraseña
        return Map.of("error", "Usuario o contraseña incorrectos");
    }

    @ExceptionHandler(ReglaNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> reglaNegocio(ReglaNegocioException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(SaldoAFavorRequiereConfirmacionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> saldoAFavor(SaldoAFavorRequiereConfirmacionException e) {
        return Map.of("codigo", "SALDO_A_FAVOR_REQUIERE_CONFIRMACION",
                "saldoAFavor", e.getSaldoAFavor(),
                "error", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validacion(MethodArgumentNotValidException e) {
        Map<String, String> errores = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
        return errores;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> integridad(DataIntegrityViolationException e) {
        return Map.of("error", "Ya existe un registro con esos datos");
    }
}