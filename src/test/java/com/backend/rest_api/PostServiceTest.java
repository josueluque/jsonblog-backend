package com.backend.rest_api;

import com.backend.rest_api.client.PagedPosts;
import com.backend.rest_api.client.PostsClient;
import com.backend.rest_api.domain.Post;
import com.backend.rest_api.domain.dto.PageResponse;
import com.backend.rest_api.domain.dto.PostResponseDTO;
import com.backend.rest_api.service.impl.CommentServiceImpl;
import com.backend.rest_api.service.PostService;
import com.backend.rest_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private CommentServiceImpl commentServiceImpl;

    @Mock
    private PostsClient postsClient;

    private PostService postService() {
        return new PostService(userService, commentServiceImpl, postsClient);
    }

    private Post post(int id, int userId, String title, String body) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(userId);
        post.setTitle(title);
        post.setBody(body);
        return post;
    }

    @Test
    void getPosts_whenFirstPage_returnsPageMetadata() {
        when(postsClient.fetchPosts(0, 10)).thenReturn(new PagedPosts(new Post[]{
                post(1, 1, "t1", "b1"),
                post(2, 1, "t2", "b2")
        }, 100));

        PageResponse<Post> result = postService().getPosts(0, 10);

        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(100);
        assertThat(result.getTotalPages()).isEqualTo(10);
        assertThat(result.getContent()).extracting(Post::getId).containsExactly(1, 2);
        assertThat(result.getPrev()).isNull();
        assertThat(result.getNext()).isEqualTo("/api/posts?page=1&size=10");
    }

    @Test
    void getPosts_whenMiddlePage_returnsPrevAndNextLinks() {
        when(postsClient.fetchPosts(4, 10)).thenReturn(new PagedPosts(new Post[]{
                post(41, 4, "t41", "b41")
        }, 100));

        PageResponse<Post> result = postService().getPosts(4, 10);

        assertThat(result.getContent()).extracting(Post::getId).containsExactly(41);
        assertThat(result.getPrev()).isEqualTo("/api/posts?page=3&size=10");
        assertThat(result.getNext()).isEqualTo("/api/posts?page=5&size=10");
    }

    @Test
    void getPosts_whenLastPage_nextIsNull() {
        when(postsClient.fetchPosts(9, 10)).thenReturn(new PagedPosts(new Post[]{
                post(100, 5, "t100", "b100")
        }, 100));

        PageResponse<Post> result = postService().getPosts(9, 10);

        assertThat(result.getContent()).extracting(Post::getId).containsExactly(100);
        assertThat(result.getPrev()).isEqualTo("/api/posts?page=8&size=10");
        assertThat(result.getNext()).isNull();
    }

    @Test
    void getPosts_whenTotalNotMultipleOfSize_roundsTotalPagesUp() {
        when(postsClient.fetchPosts(0, 10)).thenReturn(new PagedPosts(new Post[]{
                post(1, 1, "t1", "b1")
        }, 27));

        PageResponse<Post> result = postService().getPosts(0, 10);

        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getTotalElements()).isEqualTo(27);
    }

    @Test
    void getPosts_whenPageOutOfRange_returnsEmptyContent() {
        when(postsClient.fetchPosts(50, 10)).thenReturn(new PagedPosts(new Post[0], 100));

        PageResponse<Post> result = postService().getPosts(50, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(100);
        assertThat(result.getTotalPages()).isEqualTo(10);
        assertThat(result.getPrev()).isEqualTo("/api/posts?page=49&size=10");
        assertThat(result.getNext()).isNull();
    }

    @Test
    void getPosts_whenExternalTotalIsZero_returnsEmptyPage() {
        when(postsClient.fetchPosts(0, 10)).thenReturn(new PagedPosts(new Post[0], 0));

        PageResponse<Post> result = postService().getPosts(0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.getPrev()).isNull();
        assertThat(result.getNext()).isNull();
    }

    @Test
    void getPosts_whenExternalBodyIsNull_returnsEmptyPage() {
        when(postsClient.fetchPosts(0, 10)).thenReturn(new PagedPosts(null, 0));

        PageResponse<Post> result = postService().getPosts(0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void toPostResponseDTO_whenValidPost_mapsOnlyExpectedFields() {
        Post post = post(7, 3, "titulo", "cuerpo");

        PostResponseDTO dto = postService().toPostResponseDTO(post);

        assertThat(dto.getId()).isEqualTo(7);
        assertThat(dto.getTitle()).isEqualTo("titulo");
        assertThat(dto.getBody()).isEqualTo("cuerpo");
    }
}