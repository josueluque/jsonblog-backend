package com.backend.rest_api;

import com.backend.rest_api.client.PostsClient;
import com.backend.rest_api.domain.Post;
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
    void getPosts_devuelveTodosLosPostsCuandoSizeSuperaAlTotal() {
        when(postsClient.fetchAllPosts()).thenReturn(new Post[]{
                post(1, 1, "t1", "b1"),
                post(2, 1, "t2", "b2"),
                post(3, 2, "t3", "b3")
        });

        List<Post> result = postService().getPosts(0, 10);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(2).getId()).isEqualTo(3);
    }

    @Test
    void getPosts_devuelvePaginaParcialCuandoHayMenosElementosQueSize() {
        when(postsClient.fetchAllPosts()).thenReturn(new Post[]{
                post(1, 1, "t1", "b1"),
                post(2, 2, "t2", "b2"),
                post(3, 3, "t3", "b3"),
                post(4, 4, "t4", "b4"),
                post(5, 5, "t5", "b5")
        });

        List<Post> result = postService().getPosts(0, 2);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Post::getId).containsExactly(1, 2);
    }

    @Test
    void getPosts_devuelveListaVaciaCuandoLaPaginaEstaFueraDeRango() {
        when(postsClient.fetchAllPosts()).thenReturn(new Post[]{
                post(1, 1, "t1", "b1"),
                post(2, 2, "t2", "b2"),
                post(3, 3, "t3", "b3")
        });

        List<Post> result = postService().getPosts(5, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getPosts_devuelveListaVaciaCuandoElArregloExternoEstaVacio() {
        when(postsClient.fetchAllPosts()).thenReturn(new Post[0]);

        List<Post> result = postService().getPosts(0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getPosts_devuelveListaVaciaCuandoElArregloExternoEsNull() {
        when(postsClient.fetchAllPosts()).thenReturn(null);

        List<Post> result = postService().getPosts(0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void toPostResponseDTO_mapeaSoloLosCamposEsperados() {
        Post post = post(7, 3, "titulo", "cuerpo");

        PostResponseDTO dto = postService().toPostResponseDTO(post);

        assertThat(dto.getId()).isEqualTo(7);
        assertThat(dto.getTitle()).isEqualTo("titulo");
        assertThat(dto.getBody()).isEqualTo("cuerpo");
    }
}