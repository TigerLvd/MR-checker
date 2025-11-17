package com.mr.checker.model.llm;

import lombok.Data;

/**
 * Represents a single message in an LLM conversation.
 * Contains the role (system, user, assistant) and content of the message.
 */
@Data
public class Message {

    /**
     * The role of the message sender.
     */
    private String role;

    /**
     * The content/text of the message.
     */
    private String content;

    /**
     * Creates a new system message.
     *
     * @param content the system prompt content
     * @return a new Message with system role
     */
    public static Message systemMessage(String content) {
        Message message = new Message();
        message.setRole("system");
        message.setContent(content);
        return message;
    }

    /**
     * Creates a new user message.
     *
     * @param content the user message content
     * @return a new Message with user role
     */
    public static Message userMessage(String content) {
        Message message = new Message();
        message.setRole("user");
        message.setContent(content);
        return message;
    }

    /**
     * Creates a new assistant message.
     *
     * @param content the assistant response content
     * @return a new Message with assistant role
     */
    public static Message assistantMessage(String content) {
        Message message = new Message();
        message.setRole("assistant");
        message.setContent(content);
        return message;
    }
}
