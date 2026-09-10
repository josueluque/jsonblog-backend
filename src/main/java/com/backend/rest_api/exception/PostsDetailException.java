package com.backend.rest_api.exception;

public class PostsDetailException extends DomainException {
    public PostsDetailException(Throwable cause) {
        super("Error al generar el detalle de los posts", cause);
    }
}
