package com.backend.rest_api.exception;

import com.backend.rest_api.config.RequestIdFilter;
import com.backend.rest_api.domain.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationErrors(Exception e, HttpServletRequest request) {
        log.warn("Solicitud invalida en {}: {}", request.getRequestURI(), e.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Parametros de la solicitud invalidos", request);
    }

    @ExceptionHandler({
            PostNotFoundException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e, HttpServletRequest request) {
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), e.getMessage());
        return build(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler({
            PostsDetailException.class,
            DeletePostException.class,
            ExternalPostsServiceException.class
    })
    public ResponseEntity<ErrorResponse> handleInternalErrors(RuntimeException e, HttpServletRequest request) {
        log.error("Error interno procesando {}: {}", request.getRequestURI(), e.getMessage(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .requestId(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY))
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
