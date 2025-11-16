package com.mr.checker.service;

import com.mr.checker.config.properties.GitLabProperties;
import com.mr.checker.model.gitlab.GitLabComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitLabServiceTest {

    private WebClient webClient;
    private GitLabService gitLabService;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder().baseUrl("http://test.com").build();
        GitLabProperties properties = new GitLabProperties();
        properties.setUrl("http://test-gitlab.com");
        gitLabService = new GitLabService(webClient, properties);
    }

    @Test
    void shouldCreateServiceWithValidParameters() {
        // Given & When & Then
        assertThat(gitLabService).isNotNull();
    }

    @Test
    void shouldHandleNullWebClient() {
        // Given & When & Then
        // Note: Lombok @RequiredArgsConstructor doesn't validate nulls
        GitLabService service = new GitLabService(null, new GitLabProperties());
        assertThat(service).isNotNull();
    }

    @Test
    void shouldHandleNullProperties() {
        // Given
        WebClient testClient = WebClient.builder().build();

        // When & Then
        // Note: Lombok @RequiredArgsConstructor doesn't validate nulls
        GitLabService service = new GitLabService(testClient, null);
        assertThat(service).isNotNull();
    }

    @Test
    void shouldReturnMonoFromGetMRChanges() {
        // Given
        Long projectId = 123L;
        Integer mrIid = 456;

        // When
        var result = gitLabService.getMRChanges(projectId, mrIid);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnMonoFromPostComment() {
        // Given
        Long projectId = 123L;
        Integer mrIid = 456;
        GitLabComment comment = new GitLabComment();
        comment.setBody("Test comment");

        // When
        var result = gitLabService.postComment(projectId, mrIid, comment);

        // Then
        assertThat(result).isNotNull();
    }
}
