package com.mr.checker.model.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a chat completion request for OpenAI API.
 * Contains the model, messages, and generation parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * The model to use for chat completion (e.g., "gpt-3.5-turbo", "gpt-4").
     */
    private String model;

    /**
     * A list of messages comprising the conversation so far.
     */
    private List<Message> messages;

    /**
     * Controls randomness in the response. Lower values make responses more focused and deterministic.
     */
    private Double temperature;

    /**
     * The maximum number of tokens to generate in the completion.
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;
}
