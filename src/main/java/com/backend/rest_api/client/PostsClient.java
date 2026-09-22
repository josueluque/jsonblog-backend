package com.backend.rest_api.client;

import com.backend.rest_api.domain.Post;
import com.backend.rest_api.exception.DeletePostException;
import com.backend.rest_api.exception.ExternalPostsServiceException;
import com.backend.rest_api.exception.PostNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class PostsClient {
    private static final Logger log = LoggerFactory.getLogger(PostsClient.class);
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String allposts;
    private final String postByPostId;

    public PostsClient(
            RestTemplate restTemplate,
            @Value("${jsonplaceholder.base-url:https://jsonplaceholder.typicode.com}")
            String baseUrl,
            @Value("${jsonplaceholder.posts:/posts}")
            String allposts,
            @Value("${jsonplaceholder.postByPost:/posts/{postId}}")
            String postByPostId
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.allposts = allposts;
        this.postByPostId = postByPostId;
    }
    public Post[] fetchAllPosts(){
        try {
            return restTemplate.getForObject(baseUrl + allposts, Post[].class);
        } catch (RestClientException e){
            throw new ExternalPostsServiceException(e);
        }
    }

    public PagedPosts fetchPosts(int page, int size){
        String url = baseUrl + allposts + "?_page={page}&_limit={limit}";

        try {
            ResponseEntity<Post[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, HttpEntity.EMPTY, Post[].class, page + 1, size);

            Post[] posts = response.getBody();
            return new PagedPosts(posts, extractTotalElements(response, posts));
        } catch (RestClientException e){
            throw new ExternalPostsServiceException(e);
        }
    }

    private int extractTotalElements(ResponseEntity<Post[]> response, Post[] posts) {
        String totalHeader = response.getHeaders().getFirst("X-Total-Count");
        if (totalHeader != null) {
            try {
                return Integer.parseInt(totalHeader.trim());
            } catch (NumberFormatException e) {
                log.warn("Header X-Total-Count invalido '{}'. Se usara el tamano del contenido.", totalHeader);
            }
        }
        return posts != null ? posts.length : 0;
    }

    public Post getPostByPostId(Integer postId){
        String url = baseUrl + postByPostId.replace("{postId}", postId.toString());

        try {
            return restTemplate.getForObject(url, Post.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new PostNotFoundException(postId);
        } catch (RestClientException e){
            throw new ExternalPostsServiceException(e);
        }
    }

    public Void deletePostByPostId(Integer postId){
        String url = baseUrl + postByPostId.replace("{postId}", postId.toString());

        try {
            restTemplate.delete(url);
        } catch (RestClientException e){
            throw new DeletePostException(postId, e);
        }
        return null;
    }
}
