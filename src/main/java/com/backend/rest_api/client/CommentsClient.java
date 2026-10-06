package com.backend.rest_api.client;

import com.backend.rest_api.domain.Comment;
import com.backend.rest_api.exception.ExternalPostsServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class CommentsClient {
    private static final Logger log = LoggerFactory.getLogger(CommentsClient.class);
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String commentsByPostId;
    private final String comments;

    public CommentsClient(
            RestTemplate restTemplate,
            @Value("${jsonplaceholder.base-url:https://jsonplaceholder.typicode.com}")
            String baseUrl,
            @Value("${jsonplaceholder.commentsByPostId:/posts/{postId}/comments}")
            String commentsByPostId,
            @Value("${jsonplaceholder.comments:/comments}")
            String comments
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.commentsByPostId = commentsByPostId;
        this.comments = comments;
    }

    public List<Comment> getCommentsByPostId(Integer postId) {
        String url = baseUrl + commentsByPostId.replace("{postId}", postId.toString());

        try {
            Comment[] comments = restTemplate.getForObject(url, Comment[].class);
            return comments != null ? Arrays.asList(comments) : List.of();
        } catch (RestClientException e) {
            log.error("Fallo al obtener los comentarios del post {}: {}", postId, e.getMessage());
            throw new ExternalPostsServiceException(e);
        }
    }

    public List<Comment> getComments() {
        try {
            Comment[] body = restTemplate.getForObject(baseUrl + comments, Comment[].class);
            return body != null ? Arrays.asList(body) : List.of();
        } catch (RestClientException e) {
            log.error("Fallo al obtener todos los comentarios desde {}: {}", baseUrl + comments, e.getMessage());
            throw new ExternalPostsServiceException(e);
        }
    }
}