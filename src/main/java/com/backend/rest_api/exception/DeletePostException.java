package com.backend.rest_api.exception;

public class DeletePostException extends DomainException {
    public DeletePostException(Integer id, Throwable cause) {
        super("Error al intentar eliminar post con ID " + id, cause);
    }
}