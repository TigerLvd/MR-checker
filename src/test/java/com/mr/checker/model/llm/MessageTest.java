package com.mr.checker.model.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateSystemMessage() {
        // Given
        Message message = new Message();
        message.setRole("system");
        message.setContent("You are a code review assistant specialized in Java.");

        // Then
        assertThat(message.getRole()).isEqualTo("system");
        assertThat(message.getContent()).isEqualTo("You are a code review assistant specialized in Java.");
    }

    @Test
    void shouldCreateUserMessage() {
        // Given
        Message message = new Message();
        message.setRole("user");
        message.setContent("Please analyze this code for potential issues.");

        // Then
        assertThat(message.getRole()).isEqualTo("user");
        assertThat(message.getContent()).isEqualTo("Please analyze this code for potential issues.");
    }

    @Test
    void shouldCreateAssistantMessage() {
        // Given
        Message message = new Message();
        message.setRole("assistant");
        message.setContent("I found several issues in the code...");

        // Then
        assertThat(message.getRole()).isEqualTo("assistant");
        assertThat(message.getContent()).isEqualTo("I found several issues in the code...");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        Message message = new Message();
        message.setRole("user");
        message.setContent("Review this Java method for security issues.");

        // When
        String json = objectMapper.writeValueAsString(message);

        // Then
        assertThat(json).contains("\"role\":\"user\"");
        assertThat(json).contains("\"content\":\"Review this Java method for security issues.\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        // Given
        String json = "{\"role\":\"system\",\"content\":\"You are an expert Java code reviewer.\"}";

        // When
        Message message = objectMapper.readValue(json, Message.class);

        // Then
        assertThat(message.getRole()).isEqualTo("system");
        assertThat(message.getContent()).isEqualTo("You are an expert Java code reviewer.");
    }

    @Test
    void shouldHandleEmptyContent() {
        // Given
        Message message = new Message();
        message.setRole("user");
        message.setContent("");

        // Then
        assertThat(message.getContent()).isEmpty();
    }

    @Test
    void shouldCreateSystemMessageWithFactory() {
        // When
        Message message = Message.systemMessage("You are a helpful assistant.");

        // Then
        assertThat(message.getRole()).isEqualTo("system");
        assertThat(message.getContent()).isEqualTo("You are a helpful assistant.");
    }

    @Test
    void shouldCreateUserMessageWithFactory() {
        // When
        Message message = Message.userMessage("Please review this code.");

        // Then
        assertThat(message.getRole()).isEqualTo("user");
        assertThat(message.getContent()).isEqualTo("Please review this code.");
    }

    @Test
    void shouldCreateAssistantMessageWithFactory() {
        // When
        Message message = Message.assistantMessage("The code looks good!");

        // Then
        assertThat(message.getRole()).isEqualTo("assistant");
        assertThat(message.getContent()).isEqualTo("The code looks good!");
    }
}
