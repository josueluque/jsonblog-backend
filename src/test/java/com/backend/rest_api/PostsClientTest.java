package com.backend.rest_api;

import com.backend.rest_api.client.PagedPosts;
import com.backend.rest_api.client.PostsClient;
import com.backend.rest_api.domain.Post;
import com.backend.rest_api.exception.DeletePostException;
import com.backend.rest_api.exception.ExternalPostsServiceException;
import com.backend.rest_api.exception.PostNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PostsClientTest {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final String ALL_POSTS_PATH = "/posts";
    private static final String POST_BY_ID_PATH = "/posts/{postId}";

    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    private final PostsClient postsClient = new PostsClient(restTemplate, BASE_URL, ALL_POSTS_PATH, POST_BY_ID_PATH);

    private static final String POSTS_JSON = "["
            + "{\"userId\":1,\"id\":1,\"title\":\"titulo1\",\"body\":\"cuerpo1\"},"
            + "{\"userId\":1,\"id\":2,\"title\":\"titulo2\",\"body\":\"cuerpo2\"}"
            + "]";

    private static final String POST_JSON = "{\"userId\":1,\"id\":5,\"title\":\"titulo5\",\"body\":\"cuerpo5\"}";

    private String urlFor(String path) {
        return BASE_URL + path;
    }

    @Test
    void fetchAllPosts_whenServiceOk_returnsArray() {
        server.expect(requestTo(urlFor(ALL_POSTS_PATH)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(POSTS_JSON, MediaType.APPLICATION_JSON));

        Post[] posts = postsClient.fetchAllPosts();

        assertThat(posts).hasSize(2);
        assertThat(posts[0].getId()).isEqualTo(1);
        assertThat(posts[0].getTitle()).isEqualTo("titulo1");
        assertThat(posts[1].getId()).isEqualTo(2);
        server.verify();
    }

    @Test
    void fetchAllPosts_whenServiceFails_throwsExternalPostsServiceException() {
        server.expect(requestTo(urlFor(ALL_POSTS_PATH)))
                .andRespond(withServerError());

        assertThatThrownBy(() -> postsClient.fetchAllPosts())
                .isInstanceOf(ExternalPostsServiceException.class);
        server.verify();
    }

    @Test
    void fetchPosts_whenServiceOk_returnsPostsAndTotalFromHeader() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Total-Count", "100");

        server.expect(requestTo(urlFor(ALL_POSTS_PATH + "?_page=2&_limit=10")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(POSTS_JSON, MediaType.APPLICATION_JSON)
                        .headers(responseHeaders));

        PagedPosts result = postsClient.fetchPosts(1, 10);

        assertThat(result.getPosts()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(100);
        server.verify();
    }

    @Test
    void fetchPosts_whenTotalHeaderMissing_usesBodySize() {
        server.expect(requestTo(urlFor(ALL_POSTS_PATH + "?_page=1&_limit=10")))
                .andRespond(withSuccess(POSTS_JSON, MediaType.APPLICATION_JSON));

        PagedPosts result = postsClient.fetchPosts(0, 10);

        assertThat(result.getPosts()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        server.verify();
    }

    @Test
    void fetchPosts_whenServiceFails_throwsExternalPostsServiceException() {
        server.expect(requestTo(urlFor(ALL_POSTS_PATH + "?_page=1&_limit=10")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> postsClient.fetchPosts(0, 10))
                .isInstanceOf(ExternalPostsServiceException.class);
        server.verify();
    }

    @Test
    void gestPostByPostId_whenServiceOk_returnsPost() {
        server.expect(requestTo(urlFor(POST_BY_ID_PATH.replace("{postId}", "5"))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(POST_JSON, MediaType.APPLICATION_JSON));

        Post post = postsClient.gestPostByPostId(5);

        assertThat(post.getId()).isEqualTo(5);
        assertThat(post.getUserId()).isEqualTo(1);
        assertThat(post.getTitle()).isEqualTo("titulo5");
        assertThat(post.getBody()).isEqualTo("cuerpo5");
        server.verify();
    }

    @Test
    void gestPostByPostId_whenNotFound_throwsPostNotFoundException() {
        server.expect(requestTo(urlFor(POST_BY_ID_PATH.replace("{postId}", "999"))))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> postsClient.gestPostByPostId(999))
                .isInstanceOf(PostNotFoundException.class);
        server.verify();
    }

    @Test
    void gestPostByPostId_whenServiceFails_throwsExternalPostsServiceException() {
        server.expect(requestTo(urlFor(POST_BY_ID_PATH.replace("{postId}", "5"))))
                .andRespond(withServerError());

        assertThatThrownBy(() -> postsClient.gestPostByPostId(5))
                .isInstanceOf(ExternalPostsServiceException.class);
        server.verify();
    }

    @Test
    void deletePostByPostId_whenServiceOk_completes() {
        server.expect(requestTo(urlFor(POST_BY_ID_PATH.replace("{postId}", "5"))))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        postsClient.deletePostByPostId(5);
        server.verify();
    }

    @Test
    void deletePostByPostId_whenServiceFails_throwsDeletePostException() {
        server.expect(requestTo(urlFor(POST_BY_ID_PATH.replace("{postId}", "5"))))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> postsClient.deletePostByPostId(5))
                .isInstanceOf(DeletePostException.class);
        server.verify();
    }
}