package com.backend.rest_api;

import com.backend.rest_api.controller.PostController;
import com.backend.rest_api.exception.ExternalPostsServiceException;
import com.backend.rest_api.exception.PostNotFoundException;
import com.backend.rest_api.service.PostService;
import com.backend.rest_api.service.UserService;
import com.backend.rest_api.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
class ControllerExceptionHandlerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private CommentServiceImpl commentServiceImpl;

    @MockBean
    private UserService userService;

    private final ExternalPostsServiceException externalServiceError =
            new ExternalPostsServiceException(new RuntimeException("connection reset"));

    @Test
    void getPostsDetail_whenExternalServiceFails_returnsInternalServerError() throws Exception {
        when(postService.getPosts(anyInt(), anyInt()))
                .thenThrow(externalServiceError);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(""));
    }

    @Test
    void getPostsDetail_whenSizeIsZero_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/posts").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void getPostsDetail_whenSizeIsNegative_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/posts").param("size", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void getPostsDetail_whenPageIsNegative_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/posts").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void deletePostById_whenExternalServiceFails_returnsInternalServerError() throws Exception {
        doThrow(externalServiceError)
                .when(postService).deletePostById(1);

        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(""));
    }

    @Test
    void deletePostById_whenPostNotFound_returnsNotFound() throws Exception {
        doThrow(new PostNotFoundException(999))
                .when(postService).deletePostById(999);

        mockMvc.perform(delete("/api/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }
}