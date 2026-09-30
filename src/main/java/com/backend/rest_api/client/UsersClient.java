package com.backend.rest_api.client;

import com.backend.rest_api.domain.User;
import com.backend.rest_api.exception.ExternalPostsServiceException;
import com.backend.rest_api.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class UsersClient {
    private static final Logger log = LoggerFactory.getLogger(UsersClient.class);
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String usersByUserId;

    public UsersClient(
            RestTemplate restTemplate,
            @Value("${jsonplaceholder.base-url:https://jsonplaceholder.typicode.com}")
            String baseUrl,
            @Value("${jsonplaceholder.usersByUserId:/users/{userId}}")
            String usersByUserId
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.usersByUserId = usersByUserId;
    }

    public User getUserById(Integer userId){
        String url = baseUrl + usersByUserId.replace("{userId}", userId.toString());

        try{
            return restTemplate.getForObject(url, User.class);

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Usuario {} no encontrado en el servicio externo", userId);
            throw new UserNotFoundException(userId);
        } catch (RestClientException e){
            log.error("Fallo al obtener el usuario {}: {}", userId, e.getMessage());
            throw new ExternalPostsServiceException(e);
        }

    }

}
