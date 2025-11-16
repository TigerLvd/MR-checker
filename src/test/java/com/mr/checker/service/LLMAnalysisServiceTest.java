package com.mr.checker.service;

import com.mr.checker.config.properties.LLMProperties;
import com.mr.checker.model.AnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class LLMAnalysisServiceTest {

    private WebClient webClient;
    private LLMAnalysisService llmAnalysisService;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder().baseUrl("http://test-llm.com").build();
        LLMProperties properties = new LLMProperties();
        properties.setUrl("http://test-llm.com");
        properties.setModel("test-model");
        llmAnalysisService = new LLMAnalysisService(webClient, properties);
    }

    @Test
    void shouldCreateServiceWithValidParameters() {
        // Given & When & Then
        assertThat(llmAnalysisService).isNotNull();
    }

    @Test
    void shouldHandleNullWebClient() {
        // Given & When & Then
        LLMProperties properties = new LLMProperties();
        LLMAnalysisService service = new LLMAnalysisService(null, properties);
        assertThat(service).isNotNull();
    }

    @Test
    void shouldHandleNullProperties() {
        // Given
        WebClient testClient = WebClient.builder().build();

        // When & Then
        LLMAnalysisService service = new LLMAnalysisService(testClient, null);
        assertThat(service).isNotNull();
    }

    @Test
    void shouldReturnEmptyResultForNullInput() {
        // Given
        String codeSnippet = null;

        // When
        var result = llmAnalysisService.analyzeCode(codeSnippet).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnEmptyResultForEmptyInput() {
        // Given
        String codeSnippet = "";

        // When
        var result = llmAnalysisService.analyzeCode(codeSnippet).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnEmptyResultForWhitespaceInput() {
        // Given
        String codeSnippet = "   ";

        // When
        var result = llmAnalysisService.analyzeCode(codeSnippet).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }
}