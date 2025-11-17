package com.mr.checker.config;

import com.mr.checker.config.properties.LLMProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class LLMConfigTest {

    @Test
    void shouldCreateLLMWebClientWithCorrectConfiguration() {
        // Given
        LLMProperties properties = new LLMProperties();
        properties.setUrl("http://test-llm.com");
        properties.setTimeout(60000);

        LLMConfig config = new LLMConfig(properties);

        // When
        WebClient webClient = config.llmWebClient();

        // Then
        assertThat(webClient).isNotNull();
    }

    @Test
    void shouldCreateLLMWebClientWithDefaultProperties() {
        // Given
        LLMProperties properties = new LLMProperties();
        LLMConfig config = new LLMConfig(properties);

        // When
        WebClient webClient = config.llmWebClient();

        // Then
        assertThat(webClient).isNotNull();
    }
}
