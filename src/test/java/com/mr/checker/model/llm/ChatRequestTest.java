package com.mr.checker.model.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateCompleteChatRequest() {
        // Given
        Message systemMessage = Message.systemMessage("You are a code reviewer.");
        Message userMessage = Message.userMessage("Review this code.");
        List<Message> messages = Arrays.asList(systemMessage, userMessage);

        ChatRequest request = ChatRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .temperature(0.1)
                .maxTokens(1000)
                .build();

        // Then
        assertThat(request.getModel()).isEqualTo("gpt-3.5-turbo");
        assertThat(request.getMessages()).hasSize(2);
        assertThat(request.getTemperature()).isEqualTo(0.1);
        assertThat(request.getMaxTokens()).isEqualTo(1000);
    }

    @Test
    void shouldSerializeToOpenAIFormat() throws Exception {
        // Given
        Message systemMessage = Message.systemMessage("You are a helpful assistant.");
        Message userMessage = Message.userMessage("Hello!");
        List<Message> messages = Arrays.asList(systemMessage, userMessage);

        ChatRequest request = ChatRequest.builder()
                .model("gpt-4")
                .messages(messages)
                .temperature(0.7)
                .maxTokens(150)
                .build();

        // When
        String json = objectMapper.writeValueAsString(request);

        // Then
        assertThat(json).contains("\"model\":\"gpt-4\"");
        assertThat(json).contains("\"messages\":[");
        assertThat(json).contains("\"role\":\"system\"");
        assertThat(json).contains("\"role\":\"user\"");
        assertThat(json).contains("\"temperature\":0.7");
        assertThat(json).contains("\"max_tokens\":150");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        // Given
        String json = """
                {
                  "model": "gpt-3.5-turbo",
                  "messages": [
                    {"role": "system", "content": "You are helpful."},
                    {"role": "user", "content": "Hi there!"}
                  ],
                  "temperature": 0.5,
                  "max_tokens": 200
                }
                """;

        // When
        ChatRequest request = objectMapper.readValue(json, ChatRequest.class);

        // Then
        assertThat(request.getModel()).isEqualTo("gpt-3.5-turbo");
        assertThat(request.getMessages()).hasSize(2);
        assertThat(request.getMessages().get(0).getRole()).isEqualTo("system");
        assertThat(request.getMessages().get(1).getRole()).isEqualTo("user");
        assertThat(request.getTemperature()).isEqualTo(0.5);
        assertThat(request.getMaxTokens()).isEqualTo(200);
    }

    @Test
    void shouldHandleNullOptionalFields() {
        // Given
        ChatRequest request = ChatRequest.builder()
                .model("gpt-4")
                .messages(Arrays.asList(Message.userMessage("Test")))
                .build();

        // Then
        assertThat(request.getTemperature()).isNull();
        assertThat(request.getMaxTokens()).isNull();
    }
}
