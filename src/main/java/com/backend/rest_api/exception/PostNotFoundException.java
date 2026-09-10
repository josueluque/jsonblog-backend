package com.backend.rest_api.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Integer postId) {
        super("Post no encontrado con ID " + postId);

    }
}
