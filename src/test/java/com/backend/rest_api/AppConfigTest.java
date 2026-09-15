package com.backend.rest_api;

import com.backend.rest_api.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class AppConfigTest {

    private final AppConfig appConfig = new AppConfig();

    @Test
    void restTemplate_whenTimeoutsConfigured_setsConnectAndReadTimeout() {
        RestTemplate restTemplate = appConfig.restTemplate(3000, 7000);

        SimpleClientHttpRequestFactory requestFactory =
                (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();

        assertThat(requestFactory).isInstanceOf(SimpleClientHttpRequestFactory.class);
        assertThat(ReflectionTestUtils.getField(requestFactory, "connectTimeout")).isEqualTo(3000);
        assertThat(ReflectionTestUtils.getField(requestFactory, "readTimeout")).isEqualTo(7000);
    }
}