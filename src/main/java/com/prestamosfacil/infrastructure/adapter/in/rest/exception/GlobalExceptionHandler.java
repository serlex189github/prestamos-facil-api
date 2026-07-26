package com.prestamosfacil.infrastructure.adapter.in.rest.exception;

import com.prestamosfacil.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        MontoSolicitudInvalidoException.class,
        PlazoSolicitudInvalidoException.class
    })
    public ResponseEntity<ApiError> handleSolicitudInvalida(
        RuntimeException ex,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(TipoPrestamoNoDisponibleException.class)
    public ResponseEntity<ApiError> handleTipoPrestamoNoDisponible(
        TipoPrestamoNoDisponibleException ex,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiError> handleUsuarioNoEncontrado(
        UsuarioNoEncontradoException ex,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ApiError> handleCorreoDuplicado(
        CorreoDuplicadoException ex,
        HttpServletRequest request
    ) {

        return buildError(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(DocumentoDuplicadoException.class)
    public ResponseEntity<ApiError> handleDocumentoDuplicado(
        DocumentoDuplicadoException ex,
        HttpServletRequest request
    ) {

        return buildError(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(SalarioInvalidoException.class)
    public ResponseEntity<ApiError> handleSalario(
        SalarioInvalidoException ex,
        HttpServletRequest request
    ) {

        return buildError(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        return buildError(
            HttpStatus.BAD_REQUEST,
            message,
            request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
        Exception ex,
        HttpServletRequest request
    ) {

        return buildError(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ha ocurrido un error interno.",
            request.getRequestURI()
        );
    }

    private ResponseEntity<ApiError> buildError(
        HttpStatus status,
        String message,
        String path
    ) {

        ApiError error = ApiError.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(path)
            .build();

        return ResponseEntity.status(status).body(error);
    }
}
