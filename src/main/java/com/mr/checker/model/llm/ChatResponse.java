package com.mr.checker.model.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Represents the complete response from OpenAI Chat Completion API.
 * Contains the generated choices, metadata, and helper methods for easy access.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {

    /**
     * Unique identifier for the chat completion.
     */
    private String id;

    /**
     * List of completion choices. Usually contains one choice for single completions.
     */
    private List<Choice> choices;

    /**
     * Unix timestamp of when the completion was created.
     */
    private Long created;

    /**
     * The model used to generate the completion.
     */
    private String model;

    /**
     * Returns the first choice from the response, or null if no choices are available.
     * This is a convenience method for single-choice responses.
     *
     * @return the first choice, or null if choices list is empty or null
     */
    public Choice getFirstChoice() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0);
        }
        return null;
    }
}
