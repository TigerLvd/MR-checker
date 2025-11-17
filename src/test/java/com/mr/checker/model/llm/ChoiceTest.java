package com.mr.checker.model.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChoiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeFromOpenAIJson() throws Exception {
        // Given - пример ответа OpenAI API
        String openAIJson = """
                {
                  "index": 0,
                  "message": {
                    "role": "assistant",
                    "content": "The code looks good, but consider adding null checks."
                  },
                  "finish_reason": "stop"
                }
                """;

        // When
        Choice choice = objectMapper.readValue(openAIJson, Choice.class);

        // Then
        assertThat(choice.getIndex()).isEqualTo(0);
        assertThat(choice.getMessage().getRole()).isEqualTo("assistant");
        assertThat(choice.getMessage().getContent()).isEqualTo("The code looks good, but consider adding null checks.");
        assertThat(choice.getFinishReason()).isEqualTo("stop");
    }

    @Test
    void shouldHandleDifferentFinishReasons() throws Exception {
        // Given
        String jsonWithLength = """
                {
                  "index": 1,
                  "message": {
                    "role": "assistant",
                    "content": "Partial response..."
                  },
                  "finish_reason": "length"
                }
                """;

        // When
        Choice choice = objectMapper.readValue(jsonWithLength, Choice.class);

        // Then
        assertThat(choice.getIndex()).isEqualTo(1);
        assertThat(choice.getFinishReason()).isEqualTo("length");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        Message message = Message.assistantMessage("Code review completed successfully.");
        Choice choice = new Choice();
        choice.setIndex(0);
        choice.setMessage(message);
        choice.setFinishReason("stop");

        // When
        String json = objectMapper.writeValueAsString(choice);

        // Then
        assertThat(json).contains("\"index\":0");
        assertThat(json).contains("\"message\"");
        assertThat(json).contains("\"role\":\"assistant\"");
        assertThat(json).contains("\"finish_reason\":\"stop\"");
    }
}
