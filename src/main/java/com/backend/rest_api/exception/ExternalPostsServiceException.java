package com.backend.rest_api.exception;

public class ExternalPostsServiceException extends RuntimeException {
    public ExternalPostsServiceException(Throwable cause) {
        super("Error al comunicarse con el servicio externo de posts", cause);
    }
}
