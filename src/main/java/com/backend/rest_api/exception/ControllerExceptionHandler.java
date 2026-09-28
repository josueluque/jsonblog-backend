package com.backend.rest_api.exception;

import com.backend.rest_api.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<Void> handleValidationErrors(Exception e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler({
            PostNotFoundException.class,
            UserNotFoundException.class

    })
    public ResponseEntity<Void> handlePostNotFound(RuntimeException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({
            PostsDetailException.class,
            DeletePostException.class,
            ExternalPostsServiceException.class
    })
    public ResponseEntity<Void> handleInternalErrors(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
