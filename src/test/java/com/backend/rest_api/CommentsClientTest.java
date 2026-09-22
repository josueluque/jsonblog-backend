package com.backend.rest_api;

import com.backend.rest_api.client.CommentsClient;
import com.backend.rest_api.domain.Comment;
import com.backend.rest_api.exception.ExternalPostsServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CommentsClientTest {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final String COMMENTS_BY_POST_PATH = "/posts/{postId}/comments";
    private static final String COMMENTS_PATH = "/comments";

    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    private final CommentsClient commentsClient =
            new CommentsClient(restTemplate, BASE_URL, COMMENTS_BY_POST_PATH, COMMENTS_PATH);

    private static final String COMMENTS_JSON = "["
            + "{\"postId\":1,\"id\":1,\"name\":\"nombre1\",\"email\":\"email1@test.com\",\"body\":\"cuerpo1\"},"
            + "{\"postId\":1,\"id\":2,\"name\":\"nombre2\",\"email\":\"email2@test.com\",\"body\":\"cuerpo2\"}"
            + "]";

    private String urlFor(String path) {
        return BASE_URL + path;
    }

    @Test
    void getCommentsByPostId_whenServiceOk_returnsList() {
        server.expect(requestTo(urlFor("/posts/1/comments")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COMMENTS_JSON, MediaType.APPLICATION_JSON));

        List<Comment> comments = commentsClient.getCommentsByPostId(1);

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getId()).isEqualTo(1);
        assertThat(comments.get(1).getEmail()).isEqualTo("email2@test.com");
        server.verify();
    }

    @Test
    void getCommentsByPostId_whenBodyIsNull_returnsEmptyList() {
        server.expect(requestTo(urlFor("/posts/1/comments")))
                .andRespond(withSuccess());

        List<Comment> comments = commentsClient.getCommentsByPostId(1);

        assertThat(comments).isEmpty();
        server.verify();
    }

    @Test
    void getCommentsByPostId_whenServiceFails_throwsExternalPostsServiceException() {
        server.expect(requestTo(urlFor("/posts/1/comments")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> commentsClient.getCommentsByPostId(1))
                .isInstanceOf(ExternalPostsServiceException.class);
        server.verify();
    }

    @Test
    void getComments_whenServiceOk_returnsList() {
        server.expect(requestTo(urlFor(COMMENTS_PATH)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(COMMENTS_JSON, MediaType.APPLICATION_JSON));

        List<Comment> comments = commentsClient.getComments();

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getPostId()).isEqualTo(1);
        assertThat(comments.get(1).getBody()).isEqualTo("cuerpo2");
        server.verify();
    }

    @Test
    void getComments_whenBodyIsNull_returnsEmptyList() {
        server.expect(requestTo(urlFor(COMMENTS_PATH)))
                .andRespond(withSuccess());

        List<Comment> comments = commentsClient.getComments();

        assertThat(comments).isEmpty();
        server.verify();
    }

    @Test
    void getComments_whenServiceFails_throwsExternalPostsServiceException() {
        server.expect(requestTo(urlFor(COMMENTS_PATH)))
                .andRespond(withServerError());

        assertThatThrownBy(() -> commentsClient.getComments())
                .isInstanceOf(ExternalPostsServiceException.class);
        server.verify();
    }
}