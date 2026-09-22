package com.backend.rest_api.service.impl;

import com.backend.rest_api.client.CommentsClient;
import com.backend.rest_api.domain.Comment;
import com.backend.rest_api.domain.Post;

import com.backend.rest_api.domain.dto.CommentResponseDTO;
import com.backend.rest_api.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentsClient commentsClient;
    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    public CommentServiceImpl(CommentsClient commentsClient) {
        this.commentsClient = commentsClient;
    }

    public List<Comment> getCommentsByPostId(Integer postId) {
        log.info("Obteniendo comentarios para post ID {}", postId);
        return commentsClient.getCommentsByPostId(postId);
    }

    @Cacheable(cacheNames = "comments", key = "#posts.![id]")
    public Map<Integer, List<Comment>> getCommentsByPosts(List<Post> posts){
        log.info("Obteniendo comentarios para {} posts", posts.size());

        Set<Integer> postIds = posts.stream()
                .map(Post::getId)
                .collect(Collectors.toSet());

        Map<Integer, List<Comment>> commentsMap = new HashMap<>();
        postIds.forEach(postId -> commentsMap.put(postId, new ArrayList<>()));

        List<Comment> allComments = commentsClient.getComments();

        allComments.stream()
                .filter(comment -> postIds.contains(comment.getPostId()))
                .forEach(comment -> commentsMap.get(comment.getPostId()).add(comment));

        if (allComments.isEmpty()) {
            log.info("No se obtuvieron comentarios del servicio externo");
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