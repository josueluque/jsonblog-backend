package com.backend.rest_api.service.impl;

import com.backend.rest_api.domain.Comment;
import com.backend.rest_api.domain.Post;

import com.backend.rest_api.domain.dto.CommentResponseDTO;
import com.backend.rest_api.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String commentsByPostId;
    private final String comments;
    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    public CommentServiceImpl(
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
        log.info("Obteniendo comentarios para post ID {} desde URL: {}", postId, url);
        Comment[] comments = restTemplate.getForObject(url, Comment[].class);
        return comments != null ? Arrays.asList(comments) : List.of();
    }


    public Map<Integer, List<Comment>> getCommentsByPosts(List<Post> posts){
        log.info("Obteniendo comentarios para {} posts", posts.size());
        Comment[] allComments = restTemplate.getForObject(baseUrl + comments, Comment[].class);

        if (allComments == null || allComments.length == 0) {
            log.info("No se obtuvieron comentarios del servicio externo");
            return Map.of();
        }

        Set<Integer> postIds = posts.stream()
                .map(Post::getId)
                .collect(Collectors.toSet());

        Map<Integer, List<Comment>> commentsMap = Arrays.stream(allComments)
                .filter(comment -> postIds.contains(comment.getPostId()))
                .collect(Collectors.groupingBy(Comment::getPostId));

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