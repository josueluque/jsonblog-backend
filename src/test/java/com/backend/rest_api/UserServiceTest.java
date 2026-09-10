package com.backend.rest_api;

import com.backend.rest_api.client.UsersClient;
import com.backend.rest_api.domain.Post;
import com.backend.rest_api.domain.User;
import com.backend.rest_api.domain.dto.UserResponseDTO;
import com.backend.rest_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsersClient usersClient;

    private User user(int id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Post post(int id, int userId) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(userId);
        return post;
    }

    @Test
    void toUserResponseDTO_whenValidUser_mapsOnlyExpectedFields() {
        User user = user(1, "Leanne Graham", "Sincere@april.biz");
        user.setUsername("Bret");
        user.setPhone("1-770-736-8031");

        UserResponseDTO dto = new UserService(usersClient).toUserResponseDTO(user);

        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getName()).isEqualTo("Leanne Graham");
        assertThat(dto.getEmail()).isEqualTo("Sincere@april.biz");
    }

    @Test
    void getUsersByPosts_whenUsersRepeat_doesNotDuplicateCalls() {
        when(usersClient.getUserById(1)).thenReturn(user(1, "Leanne Graham", "Sincere@april.biz"));
        when(usersClient.getUserById(2)).thenReturn(user(2, "Ervin Howell", "Shanna@melissa.tv"));

        List<Post> posts = List.of(
                post(1, 1),
                post(2, 1),
                post(3, 2)
        );

        Map<Integer, User> users = new UserService(usersClient).getUsersByPosts(posts);

        assertThat(users).hasSize(2);
        assertThat(users.get(1).getName()).isEqualTo("Leanne Graham");
        assertThat(users.get(2).getName()).isEqualTo("Ervin Howell");
        verify(usersClient, times(1)).getUserById(1);
        verify(usersClient, times(1)).getUserById(2);
    }
}