package com.backend.rest_api.service.impl;

import com.backend.rest_api.domain.Comment;
import com.backend.rest_api.domain.Post;

import com.backend.rest_api.domain.dto.CommentResponseDTO;
import com.backend.rest_api.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String commentsByPostId;
    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    public CommentServiceImpl(
            RestTemplate restTemplate,
            @Value("${jsonplaceholder.base-url:https://jsonplaceholder.typicode.com}")
            String baseUrl,
            @Value("${jsonplaceholder.commentsByPostId:/posts/{postId}/comments}")
            String commentsByPostId
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.commentsByPostId = commentsByPostId;
    }

    public List<Comment> getCommentsByPostId(Integer postId) {
        String url = baseUrl + commentsByPostId.replace("{postId}", postId.toString());
        log.info("Obteniendo comentarios para post ID {} desde URL: {}", postId, url);
        Comment[] comments = restTemplate.getForObject(url, Comment[].class);
        return comments != null ? Arrays.asList(comments) : List.of();
    }


    @Cacheable(cacheNames = "comments", key = "#posts.![id]")
    public Map<Integer, List<Comment>> getCommentsByPosts(List<Post> posts){
        log.info("Obteniendo comentarios para   {} posts", posts.size());
        Map<Integer, List<Comment>> commentsMap = new HashMap<>();
        for (Post post : posts) {
            commentsMap.put(post.getId(), getCommentsByPostId(post.getId()));
        }
        log.info("Comentarios obtenidos exitosamente");
        return commentsMap;
    }

    public CommentResponseDTO toCommentResponseDTO(Comment comment){
        CommentResponseDTO commentDto = new CommentResponseDTO();
        commentDto.setId(comment.getId());
        commentDto.setName(comment.getName());
        commentDto.setEmail(comment.getEmail());
        commentDto.setBody(comment.getBody());
        return commentDto;
    }
}