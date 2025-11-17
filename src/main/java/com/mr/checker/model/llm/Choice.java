package com.mr.checker.model.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a single choice/completion from OpenAI API response.
 * Contains the generated message and metadata about why the generation stopped.
 */
@Data
public class Choice {

    /**
     * The index of this choice in the list of choices returned by the API.
     * Usually 0 for single choice responses.
     */
    private Integer index;

    /**
     * The generated message from the LLM.
     */
    private Message message;

    /**
     * The reason why the model stopped generating tokens.
     * Common values: "stop", "length", "content_filter".
     */
    @JsonProperty("finish_reason")
    private String finishReason;
}
