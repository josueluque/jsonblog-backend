package com.backend.rest_api;

import com.backend.rest_api.client.UsersClient;
import com.backend.rest_api.domain.User;
import com.backend.rest_api.exception.ExternalPostsServiceException;
import com.backend.rest_api.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UsersClientTest {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final String USERS_BY_USER_ID_PATH = "/users/{userId}";

    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    private final UsersClient usersClient = new UsersClient(restTemplate, BASE_URL, USERS_BY_USER_ID_PATH);

    private static final String USER_JSON = "{"
            + "\"id\":1,"
            + "\"name\":\"Leanne Graham\","
            + "\"username\":\"Bret\","
            + "\"email\":\"Sincere@april.biz\""
            + "}";

    private String urlFor(String path) {
        return BASE_URL + path;
    }

    @Test
    void getUserById_devuelveElUsuarioBuscado() {
        server.expect(requestTo(urlFor(USERS_BY_USER_ID_PATH.replace("{userId}", "1"))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(USER_JSON, MediaType.APPLICATION_JSON));

        User user = usersClient.getUserById(1);

        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getName()).isEqualTo("Leanne Graham");
        assertThat(user.getUsername()).isEqualTo("Bret");
        assertThat(user.getEmail()).isEqualTo("Sincere@april.biz");
        server.verify();
    }

    @Test
    void getUserById_lanzaUserNotFoundCuandoResponde404() {
        server.expect(requestTo(urlFor(USERS_BY_USER_ID_PATH.replace("{userId}", "999"))))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> usersClient.getUserById(999))
                .isInstanceOf(UserNotFoundException.class);
        server.verify();
    }

    @Test
    void getUserById_lanzaExcepcionExternaCuandoRespondeError() {
        server.expect(requestTo(urlFor(USERS_BY_USER_ID_PATH.replace("{userId}", "1"))))
                .andRespond(withServerError());

        assertThatThrownBy(() -> usersClient.getUserById(1))
                .isInstanceOf(ExternalPostsServiceException.class);
        server.verify();
    }
}