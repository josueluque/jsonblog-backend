package com.backend.rest_api.service;

import com.backend.rest_api.client.UsersClient;
import com.backend.rest_api.domain.Post;
import com.backend.rest_api.domain.User;
import com.backend.rest_api.domain.dto.UserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UsersClient usersClient;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(
            UsersClient usersClient
    ) {
        this.usersClient = usersClient;
    }

    public User getUserById(Integer userId) {
        log.info("Obteniendo usuario con ID {}", userId);

        return usersClient.getUserById(userId);
    }

    @Cacheable(cacheNames = "users", key = "#posts.![userId]")
    public Map<Integer, User> getUsersByPosts(List<Post> posts) {
        log.info("Obteniendo usuarios para {} posts", posts.size());
        Map<Integer, User> users = new HashMap<>();
        for (Post post : posts) {
            users.computeIfAbsent(post.getUserId(), this::getUserById);
        }
        log.info("Usuarios obtenidos: {}", users.size());
        return users;
    }

    public UserResponseDTO toUserResponseDTO(User user){
        log.debug("Convirtiendo usuario {} a DTO", user.getId());
        UserResponseDTO userDto = new UserResponseDTO();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

}
