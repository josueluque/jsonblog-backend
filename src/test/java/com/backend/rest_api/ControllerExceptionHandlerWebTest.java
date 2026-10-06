package com.backend.rest_api;

import com.backend.rest_api.controller.PostController;
import com.backend.rest_api.domain.dto.CommentResponseDTO;
import com.backend.rest_api.domain.dto.DetailResponseDTO;
import com.backend.rest_api.domain.dto.PageResponse;
import com.backend.rest_api.domain.dto.PostResponseDTO;
import com.backend.rest_api.domain.dto.UserResponseDTO;
import com.backend.rest_api.exception.ExternalPostsServiceException;
import com.backend.rest_api.exception.PostNotFoundException;
import com.backend.rest_api.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
class ControllerExceptionHandlerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    private final ExternalPostsServiceException externalServiceError =
            new ExternalPostsServiceException(new RuntimeException("connection reset"));

    @Test
    void getPostsDetail_whenExternalServiceFails_returnsInternalServerError() throws Exception {
        when(postService.getPostsDetailPage(anyInt(), anyInt()))
                .thenThrow(externalServiceError);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.path").value("/api/posts"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getPostsDetail_whenNoPosts_returnsNoContent() throws Exception {
        when(postService.getPostsDetailPage(anyInt(), anyInt()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void getPostsDetail_whenHasPosts_returnsOkWithDetail() throws Exception {
        DetailResponseDTO detail = new DetailResponseDTO();
        PostResponseDTO post = new PostResponseDTO();
        post.setId(1);
        post.setTitle("titulo");
        detail.setPost(post);
        UserResponseDTO user = new UserResponseDTO();
        user.setId(1);
        detail.setUser(user);
        CommentResponseDTO comment = new CommentResponseDTO();
        comment.setId(1);
        detail.setComments(List.of(comment));

        PageResponse<DetailResponseDTO> page =
                new PageResponse<>(0, 10, 100, 10, List.of(detail), null, "/api/posts?page=1&size=10");

        when(postService.getPostsDetailPage(anyInt(), anyInt()))
                .thenReturn(Optional.of(page));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.content[0].post.id").value(1))
                .andExpect(jsonPath("$.content[0].user.id").value(1))
                .andExpect(jsonPath("$.content[0].comments[0].id").value(1));
    }

    @Test
    void getPostsDetail_whenSizeIsZero_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/posts").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getPostsDetail_whenSizeIsNegative_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/posts").param("size", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getPostsDetail_whenPageIsNegative_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/posts").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getPostsDetail_whenSizeExceedsMax_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/posts").param("size", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void corsPreflight_fromAllowedOrigin_isAllowed() throws Exception {
        mockMvc.perform(options("/api/posts")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void deletePostById_whenExternalServiceFails_returnsInternalServerError() throws Exception {
        doThrow(externalServiceError)
                .when(postService).deletePostById(1);

        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.path").value("/api/posts/1"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void deletePostById_whenPostNotFound_returnsNotFound() throws Exception {
        doThrow(new PostNotFoundException(999))
                .when(postService).deletePostById(999);

        mockMvc.perform(delete("/api/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/posts/999"))
                .andExpect(jsonPath("$.requestId").exists());
    }
}