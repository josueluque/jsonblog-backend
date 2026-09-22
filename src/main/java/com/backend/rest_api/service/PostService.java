package com.backend.rest_api.service;

import com.backend.rest_api.client.PagedPosts;
import com.backend.rest_api.client.PostsClient;
import com.backend.rest_api.domain.Comment;
import com.backend.rest_api.domain.Post;
import com.backend.rest_api.domain.User;
import com.backend.rest_api.domain.dto.DetailResponseDTO;
import com.backend.rest_api.domain.dto.PageResponse;
import com.backend.rest_api.domain.dto.PostResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PostService {
    private final UserService userService;
    private final CommentService commentServiceImpl;

    private final PostsClient postsClient;
    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private static final String POSTS_PATH = "/api/posts";

    public PostService(
            UserService userService,
            CommentService commentServiceImpl,
            PostsClient postsClient
    ) {
        this.userService = userService;
        this.commentServiceImpl = commentServiceImpl;
        this.postsClient = postsClient;
    }

    public PageResponse<Post> getPosts(
            int page,
            int size
    ) {
        log.info("Obteniendo posts pagina {} (size {})", page, size);
        PagedPosts pagedPosts = postsClient.fetchPosts(page, size);

        Post[] posts = pagedPosts.getPosts();
        List<Post> postsList = posts != null ? Arrays.asList(posts) : List.of();

        if (postsList.isEmpty()) {
            log.info("No se encontraron posts");
        }

        int totalElements = pagedPosts.getTotalElements();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        String prev = page > 0 ? POSTS_PATH + "?page=" + (page - 1) + "&size=" + size : null;
        String next = page + 1 < totalPages ? POSTS_PATH + "?page=" + (page + 1) + "&size=" + size : null;

        return new PageResponse<>(page, size, totalElements, totalPages, postsList, prev, next);
    }


    public Optional<PageResponse<DetailResponseDTO>> getPostsDetailPage(int page, int size) {
        PageResponse<Post> postsPage = getPosts(page, size);

        if (postsPage.getContent().isEmpty()) {
            log.info("No hay posts para el detalle (pagina {}, size {})", page, size);
            return Optional.empty();
        }

        List<Post> posts = postsPage.getContent();

        Map<Integer, List<Comment>> postsComments = commentServiceImpl.getCommentsByPosts(posts);

        Map<Integer, User> usersMap = userService.getUsersByPosts(posts);

        List<DetailResponseDTO> postsDetail = getPostsDetail(posts, postsComments, usersMap);

        PageResponse<DetailResponseDTO> response = new PageResponse<>(
                postsPage.getPage(), postsPage.getSize(), postsPage.getTotalElements(),
                postsPage.getTotalPages(), postsDetail, postsPage.getPrev(), postsPage.getNext());

        return Optional.of(response);
    }

    public PostResponseDTO toPostResponseDTO(Post post){
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setBody(post.getBody());
        return dto;
    }

    private DetailResponseDTO toDetailResponseDTO (
            Post post,
            Map<Integer, List<Comment>> comments,
            Map<Integer, User> usersMap)
    {
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

    private List<DetailResponseDTO> getPostsDetail(
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

        postsClient.getPostByPostId(postId);
        postsClient.deletePostByPostId(postId);

        log.info("Post con ID {} eliminado correctamente", postId);
        return null;
    }

}
