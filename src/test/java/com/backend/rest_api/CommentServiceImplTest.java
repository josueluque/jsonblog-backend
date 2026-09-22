package com.backend.rest_api;

import com.backend.rest_api.client.CommentsClient;
import com.backend.rest_api.domain.Comment;
import com.backend.rest_api.domain.Post;
import com.backend.rest_api.domain.dto.CommentResponseDTO;
import com.backend.rest_api.service.CommentService;
import com.backend.rest_api.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentsClient commentsClient;

    private CommentServiceImpl commentService() {
        return new CommentServiceImpl(commentsClient);
    }

    private Comment comment(int id, int postId, String name, String email, String body) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setPostId(postId);
        comment.setName(name);
        comment.setEmail(email);
        comment.setBody(body);
        return comment;
    }

    @Test
    void getCommentsByPostId_whenServiceReturnsComments_returnsList() {
        when(commentsClient.getCommentsByPostId(1)).thenReturn(List.of(
                comment(1, 1, "comentario 1", "email1@test.com", "cuerpo 1"),
                comment(2, 1, "comentario 2", "email2@test.com", "cuerpo 2")
        ));

        List<Comment> result = commentService().getCommentsByPostId(1);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1).getEmail()).isEqualTo("email2@test.com");
    }

    @Test
    void getCommentsByPostId_whenResponseIsEmpty_returnsEmptyList() {
        when(commentsClient.getCommentsByPostId(1)).thenReturn(List.of());

        List<Comment> result = commentService().getCommentsByPostId(1);

        assertThat(result).isEmpty();
    }

    @Test
    void getCommentsByPosts_whenMultiplePosts_groupsCommentsByPostId() {
        when(commentsClient.getComments()).thenReturn(List.of(
                comment(1, 1, "c1", "e1", "b1"),
                comment(2, 2, "c2", "e2", "b2"),
                comment(3, 5, "c3", "e3", "b3")
        ));

        List<Post> posts = List.of(post(1), post(2));

        Map<Integer, List<Comment>> result = commentService().getCommentsByPosts(posts);

        assertThat(result).hasSize(2);
        assertThat(result.get(1)).hasSize(1);
        assertThat(result.get(1).get(0).getId()).isEqualTo(1);
        assertThat(result.get(2)).hasSize(1);
        assertThat(result.get(2).get(0).getId()).isEqualTo(2);
        assertThat(result).doesNotContainKey(5);
    }

    @Test
    void getCommentsByPosts_whenNoComments_returnsEmptyListsForPosts() {
        when(commentsClient.getComments()).thenReturn(List.of());

        Map<Integer, List<Comment>> result = commentService().getCommentsByPosts(List.of(post(1), post(2)));

        assertThat(result).hasSize(2);
        assertThat(result.get(1)).isEmpty();
        assertThat(result.get(2)).isEmpty();
    }

    @Test
    void getCommentsByPosts_whenPostHasNoComments_keepsEmptyListForKey() {
        when(commentsClient.getComments()).thenReturn(List.of(comment(1, 1, "c1", "e1", "b1")));

        List<Post> posts = List.of(post(1), post(2));

        Map<Integer, List<Comment>> result = commentService().getCommentsByPosts(posts);

        assertThat(result).hasSize(2);
        assertThat(result.get(1)).hasSize(1);
        assertThat(result.get(2)).isEmpty();
    }

    @Test
    void toCommentResponseDTO_whenValidComment_mapsOnlyExpectedFields() {
        Comment comment = comment(3, 1, "nombre", "email@test.com", "cuerpo");

        CommentResponseDTO dto = commentService().toCommentResponseDTO(comment);

        assertThat(dto.getId()).isEqualTo(3);
        assertThat(dto.getName()).isEqualTo("nombre");
        assertThat(dto.getEmail()).isEqualTo("email@test.com");
        assertThat(dto.getBody()).isEqualTo("cuerpo");
    }

    @Test
    void commentService_whenInstantiated_implementsCommentServiceInterface() {
        CommentService commentService = commentService();

        assertThat(commentService).isInstanceOf(CommentService.class);
    }

    private Post post(int id) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(1);
        return post;
    }
}