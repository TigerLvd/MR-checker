package com.mr.checker.model.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ChatResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeCompleteOpenAIResponse() throws Exception {
        // Given - полный пример ответа OpenAI API
        String openAIJson = """
                {
                  "id": "chatcmpl-123",
                  "object": "chat.completion",
                  "created": 1677652288,
                  "model": "gpt-3.5-turbo-0125",
                  "choices": [
                    {
                      "index": 0,
                      "message": {
                        "role": "assistant",
                        "content": "The code has potential security issues. Consider using prepared statements."
                      },
                      "finish_reason": "stop"
                    }
                  ],
                  "usage": {
                    "prompt_tokens": 9,
                    "completion_tokens": 12,
                    "total_tokens": 21
                  }
                }
                """;

        // When
        ChatResponse response = objectMapper.readValue(openAIJson, ChatResponse.class);

        // Then
        assertThat(response.getId()).isEqualTo("chatcmpl-123");
        assertThat(response.getCreated()).isEqualTo(1677652288L);
        assertThat(response.getModel()).isEqualTo("gpt-3.5-turbo-0125");
        assertThat(response.getChoices()).hasSize(1);

        Choice choice = response.getChoices().get(0);
        assertThat(choice.getIndex()).isEqualTo(0);
        assertThat(choice.getMessage().getRole()).isEqualTo("assistant");
        assertThat(choice.getFinishReason()).isEqualTo("stop");
    }

    @Test
    void shouldGetFirstChoice() throws Exception {
        // Given
        Choice choice1 = new Choice();
        choice1.setIndex(0);
        choice1.setMessage(Message.assistantMessage("First choice"));
        choice1.setFinishReason("stop");

        Choice choice2 = new Choice();
        choice2.setIndex(1);
        choice2.setMessage(Message.assistantMessage("Second choice"));
        choice2.setFinishReason("stop");

        ChatResponse response = new ChatResponse();
        response.setChoices(Arrays.asList(choice1, choice2));

        // When & Then
        assertThat(response.getFirstChoice()).isEqualTo(choice1);
        assertThat(response.getFirstChoice().getMessage().getContent()).isEqualTo("First choice");
    }

    @Test
    void shouldHandleEmptyChoices() throws Exception {
        // Given
        ChatResponse response = new ChatResponse();
        response.setChoices(Arrays.asList());

        // When & Then
        assertThat(response.getFirstChoice()).isNull();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        Choice choice = new Choice();
        choice.setIndex(0);
        choice.setMessage(Message.assistantMessage("Code review completed."));
        choice.setFinishReason("stop");

        ChatResponse response = new ChatResponse();
        response.setId("chatcmpl-456");
        response.setCreated(1677652400L);
        response.setModel("gpt-4");
        response.setChoices(Arrays.asList(choice));

        // When
        String json = objectMapper.writeValueAsString(response);

        // Then
        assertThat(json).contains("\"id\":\"chatcmpl-456\"");
        assertThat(json).contains("\"created\":1677652400");
        assertThat(json).contains("\"model\":\"gpt-4\"");
        assertThat(json).contains("\"choices\":[");
    }
}
