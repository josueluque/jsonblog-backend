package com.backend.rest_api.exception;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(Integer postId) {
        super("Usuario no encontrado con ID " + postId);

    }

}
