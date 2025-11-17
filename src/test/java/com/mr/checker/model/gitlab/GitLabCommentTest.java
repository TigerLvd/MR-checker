package com.mr.checker.model.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitLabCommentTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateCommentWithBody() {
        // Given
        GitLabComment comment = new GitLabComment();
        comment.setBody("This merge request looks good! ✅");

        // Then
        assertThat(comment.getBody()).isEqualTo("This merge request looks good! ✅");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        GitLabComment comment = new GitLabComment();
        comment.setBody("## Code Review Results\n\n- ✅ No critical issues found\n- ⚠️ Consider adding more tests");

        // When
        String json = objectMapper.writeValueAsString(comment);

        // Then
        assertThat(json).contains("\"body\":\"## Code Review Results");
        assertThat(json).contains("No critical issues found");
        assertThat(json).contains("Consider adding more tests");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        // Given
        String json = "{\"body\":\"MR approved! 🚀\"}";

        // When
        GitLabComment comment = objectMapper.readValue(json, GitLabComment.class);

        // Then
        assertThat(comment.getBody()).isEqualTo("MR approved! 🚀");
    }

    @Test
    void shouldHandleEmptyBody() {
        // Given
        GitLabComment comment = new GitLabComment();
        comment.setBody("");

        // Then
        assertThat(comment.getBody()).isEmpty();
    }

    @Test
    void shouldHandleNullBody() {
        // Given
        GitLabComment comment = new GitLabComment();
        comment.setBody(null);

        // Then
        assertThat(comment.getBody()).isNull();
    }
}
