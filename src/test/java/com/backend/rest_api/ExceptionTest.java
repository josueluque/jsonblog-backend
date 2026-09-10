package com.backend.rest_api;

import com.backend.rest_api.exception.DeletePostException;
import com.backend.rest_api.exception.ExternalPostsServiceException;
import com.backend.rest_api.exception.PostNotFoundException;
import com.backend.rest_api.exception.PostsDetailException;
import com.backend.rest_api.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void postNotFoundException_incluyeElIdEnElMensaje() {
        PostNotFoundException ex = new PostNotFoundException(42);

        assertThat(ex.getMessage()).contains("42");
    }

    @Test
    void userNotFoundException_incluyeElIdEnElMensaje() {
        UserNotFoundException ex = new UserNotFoundException(7);

        assertThat(ex.getMessage()).contains("7");
    }

    @Test
    void externalPostsServiceException_conservaLaCausa() {
        RuntimeException cause = new RuntimeException("timeout");

        ExternalPostsServiceException ex = new ExternalPostsServiceException(cause);

        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex.getMessage()).contains("servicio externo");
    }

    @Test
    void deletePostException_conservaLaCausaYElId() {
        RuntimeException cause = new RuntimeException("connection reset");

        DeletePostException ex = new DeletePostException(10, cause);

        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex.getMessage()).contains("10");
    }

    @Test
    void postsDetailException_conservaLaCausa() {
        RuntimeException cause = new RuntimeException("parse error");

        PostsDetailException ex = new PostsDetailException(cause);

        assertThat(ex.getCause()).isSameAs(cause);
    }
}