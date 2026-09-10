package com.backend.rest_api.exception;

import com.backend.rest_api.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler({
            PostNotFoundException.class,
            UserNotFoundException.class

    })
    public ResponseEntity<Void> handlePostNotFound(RuntimeException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({
            PostsDetailException.class,
            DeletePostException.class
    })
    public ResponseEntity<Void> handleInternalErrors(DomainException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
