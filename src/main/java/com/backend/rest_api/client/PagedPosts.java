package com.backend.rest_api.client;

import com.backend.rest_api.domain.Post;

public class PagedPosts {
    private Post[] posts;
    private int totalElements;

    public PagedPosts() {
    }

    public PagedPosts(Post[] posts, int totalElements) {
        this.posts = posts;
        this.totalElements = totalElements;
    }

    public Post[] getPosts() {
        return posts;
    }

    public void setPosts(Post[] posts) {
        this.posts = posts;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }
}