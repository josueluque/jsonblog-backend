package com.backend.rest_api.service;

import com.backend.rest_api.client.PostsClient;
import com.backend.rest_api.domain.Comment;
import com.backend.rest_api.domain.Post;
import com.backend.rest_api.domain.User;
import com.backend.rest_api.domain.dto.DetailResponseDTO;
import com.backend.rest_api.domain.dto.PostResponseDTO;
import com.backend.rest_api.service.impl.CommentServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class PostService {
    private final UserService userService;
    private final CommentServiceImpl commentServiceImpl;

    private final PostsClient postsClient;
    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    public PostService(
            UserService userService,
            CommentServiceImpl commentServiceImpl,
            PostsClient postsClient
    ) {
        this.userService = userService;
        this.commentServiceImpl = commentServiceImpl;
        this.postsClient = postsClient;
    }

    public List<Post> getPosts(
            int page,
            int size
    ) {
        log.info("Obteniendo todos los posteos");
        Post[] allPostsArray = postsClient.fetchAllPosts();;

        if (allPostsArray == null || allPostsArray.length == 0){
            log.info("No se encontraron posts");
            return List.of();
        }

        List<Post> postsList = Arrays.asList(allPostsArray);

        return paginate(postsList, page, size);
    }

    private <T> List<T> paginate(List<T> list, int page, int size) {

        int fromIndex = page * size;

        if (fromIndex >= list.size()) {
            log.info("Pagina {} fuera de rango para {} elementos", page, size);
            return List.of();
        }

        int toIndex = Math.min(fromIndex + size, list.size());
        return list.subList(fromIndex, toIndex);
    }


    public PostResponseDTO toPostResponseDTO(Post post){
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setBody(post.getBody());
        return dto;
    }

    public DetailResponseDTO toDetailResponseDTO (
            Post post,
            Map<Integer, List<Comment>> comments,
            Map<Integer, User> usersMap)
    {
//        log.info("Convirtiendo Post {}, user {} y comentarios a DetailResponseDTO", post.getId(), post.getUserId());
        User user = usersMap.get(post.getUserId());
        List<Comment> commentsList = comments.get(post.getId());

        DetailResponseDTO response = new DetailResponseDTO();
        response.setPost(toPostResponseDTO(post));
        response.setUser(userService.toUserResponseDTO(user));
        response.setComments(
                commentsList.stream()
                        .map(commentServiceImpl::toCommentResponseDTO)
                        .toList()
        );

        log.info("Detalle de posts generados");
        return response;
    }

    public List<DetailResponseDTO> getPostsDetail(
            List<Post> posts,
            Map<Integer, List<Comment>> comments,
            Map<Integer, User> usersMap
    ){
        log.info("Generando detalle de posts");

        return posts.stream()
                .map(post -> toDetailResponseDTO(post, comments, usersMap))
                .toList();
    }

    public Void deletePostById(Integer postId){
        log.info("Eliminando post con ID {}", postId);

        postsClient.gestPostByPostId(postId);
        postsClient.deletePostByPostId(postId);

        log.info("Post con ID {} eliminado correctamente", postId);
        return null;
    }

}
