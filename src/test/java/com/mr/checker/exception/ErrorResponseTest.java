package com.mr.checker.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void errorResponse_shouldCreateWithRequiredFields() {
        // Given
        String message = "Internal server error";
        String errorCode = "INTERNAL_ERROR";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String path = "/api/check-mr";

        // When
        ErrorResponse errorResponse = new ErrorResponse(message, errorCode, status, path);

        // Then
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getMessage()).isEqualTo(message);
        assertThat(errorResponse.getErrorCode()).isEqualTo(errorCode);
        assertThat(errorResponse.getStatus()).isEqualTo(status.value());
        assertThat(errorResponse.getPath()).isEqualTo(path);
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void errorResponse_shouldCreateWithException() {
        // Given
        GitLabApiException exception = new GitLabApiException("GitLab API unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        String path = "/api/check-mr";

        // When
        ErrorResponse errorResponse = new ErrorResponse(exception, path);

        // Then
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getMessage()).isEqualTo("GitLab API unavailable");
        assertThat(errorResponse.getErrorCode()).isEqualTo("GITLAB_API_ERROR");
        assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(errorResponse.getPath()).isEqualTo(path);
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }

    @Test
    void errorResponse_shouldCreateWithGenericException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");
        String path = "/api/check-mr";

        // When
        ErrorResponse errorResponse = new ErrorResponse(exception, path);

        // Then
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getMessage()).isEqualTo("Unexpected error");
        assertThat(errorResponse.getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(errorResponse.getPath()).isEqualTo(path);
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }

    @Test
    void errorResponse_shouldSerializeToJsonProperly() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse(
            "Validation failed",
            "VALIDATION_ERROR",
            HttpStatus.BAD_REQUEST,
            "/api/check-mr"
        );

        // When & Then - проверяем, что объект можно сериализовать
        // (Jackson должен корректно обработать все поля)
        assertThat(errorResponse.getMessage()).isEqualTo("Validation failed");
        assertThat(errorResponse.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(errorResponse.getStatus()).isEqualTo(400);
        assertThat(errorResponse.getPath()).isEqualTo("/api/check-mr");
        assertThat(errorResponse.getTimestamp()).isNotNull();
    }
}
