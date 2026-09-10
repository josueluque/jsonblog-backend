package com.backend.rest_api.service;

import com.backend.rest_api.domain.Comment;
import com.backend.rest_api.domain.Post;
import com.backend.rest_api.domain.dto.CommentResponseDTO;

import java.util.List;
import java.util.Map;

public interface CommentSerivce {

    List<Comment> getCommentsByPostId(Integer postId);

    Map<Integer, List<Comment>> getCommentsByPosts(List<Post> posts);

    CommentResponseDTO toCommentResponseDTO(Comment comment);

}
