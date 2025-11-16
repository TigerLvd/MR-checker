package com.mr.checker.config;

import com.mr.checker.config.properties.GitLabProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class GitLabConfigTest {

    @Test
    void shouldCreateGitLabWebClientWithCorrectConfiguration() {
        // Given
        GitLabProperties properties = new GitLabProperties();
        properties.setUrl("http://test-gitlab.com");
        properties.setToken("test-token-123");
        properties.setTimeout(15000);

        GitLabConfig config = new GitLabConfig(properties);

        // When
        WebClient webClient = config.gitLabWebClient();

        // Then
        assertThat(webClient).isNotNull();
    }

    @Test
    void shouldCreateGitLabWebClientWithDefaultProperties() {
        // Given
        GitLabProperties properties = new GitLabProperties();
        GitLabConfig config = new GitLabConfig(properties);

        // When
        WebClient webClient = config.gitLabWebClient();

        // Then
        assertThat(webClient).isNotNull();
    }
}

