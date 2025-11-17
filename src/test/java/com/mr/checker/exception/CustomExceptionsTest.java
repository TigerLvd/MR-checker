package com.mr.checker.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class CustomExceptionsTest {

    @Test
    void gitLabApiException_shouldCreateWithMessage() {
        // Given
        String message = "Failed to connect to GitLab API";

        // When
        GitLabApiException exception = new GitLabApiException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void gitLabApiException_shouldCreateWithMessageAndCause() {
        // Given
        String message = "GitLab API returned error";
        Throwable cause = new RuntimeException("Connection timeout");

        // When
        GitLabApiException exception = new GitLabApiException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void gitLabApiException_shouldCreateWithMessageAndHttpStatus() {
        // Given
        String message = "GitLab API authentication failed";
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        // When
        GitLabApiException exception = new GitLabApiException(message, status);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
    }

    @Test
    void gitLabApiException_shouldCreateWithMessageCauseAndHttpStatus() {
        // Given
        String message = "GitLab API server error";
        Throwable cause = new RuntimeException("Server error");
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // When
        GitLabApiException exception = new GitLabApiException(message, cause, status);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
    }

    @Test
    void gitLabApiException_shouldHaveCorrectDefaultHttpStatus() {
        // Given
        String message = "Generic GitLab error";

        // When
        GitLabApiException exception = new GitLabApiException(message);

        // Then
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void LLMApiException_shouldCreateWithMessage() {
        // Given
        String message = "Failed to connect to LLM API";

        // When
        LLMApiException exception = new LLMApiException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void LLMApiException_shouldCreateWithMessageAndCause() {
        // Given
        String message = "LLM API returned error";
        Throwable cause = new RuntimeException("Connection timeout");

        // When
        LLMApiException exception = new LLMApiException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void LLMApiException_shouldCreateWithMessageAndHttpStatus() {
        // Given
        String message = "LLM API authentication failed";
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        // When
        LLMApiException exception = new LLMApiException(message, status);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
    }

    @Test
    void LLMApiException_shouldCreateWithMessageCauseAndHttpStatus() {
        // Given
        String message = "LLM API server error";
        Throwable cause = new RuntimeException("Server error");
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // When
        LLMApiException exception = new LLMApiException(message, cause, status);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
    }

    @Test
    void LLMApiException_shouldHaveCorrectDefaultHttpStatus() {
        // Given
        String message = "Generic LLM error";

        // When
        LLMApiException exception = new LLMApiException(message);

        // Then
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void MRCheckException_shouldCreateWithMessage() {
        // Given
        String message = "General MR check error";

        // When
        MRCheckException exception = new MRCheckException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void MRCheckException_shouldCreateWithMessageAndCause() {
        // Given
        String message = "MR check processing error";
        Throwable cause = new RuntimeException("Processing failed");

        // When
        MRCheckException exception = new MRCheckException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void MRCheckException_shouldCreateWithMessageAndHttpStatus() {
        // Given
        String message = "MR check validation error";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // When
        MRCheckException exception = new MRCheckException(message, status);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
    }

    @Test
    void MRCheckException_shouldCreateWithMessageCauseAndHttpStatus() {
        // Given
        String message = "MR check internal error";
        Throwable cause = new RuntimeException("Internal failure");
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // When
        MRCheckException exception = new MRCheckException(message, cause, status);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getHttpStatus()).isEqualTo(status);
    }

    @Test
    void MRCheckException_shouldHaveCorrectDefaultHttpStatus() {
        // Given
        String message = "Generic MR check error";

        // When
        MRCheckException exception = new MRCheckException(message);

        // Then
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
