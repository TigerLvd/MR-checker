package com.mr.checker.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("null")
class GlobalExceptionHandlerTest {

    private final Environment environment = mock(Environment.class);
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(environment);

    private ServerWebExchange createMockExchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path));
    }

    @Test
    void handleGitLabApiException_shouldReturnCorrectResponse() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        GitLabApiException exception = new GitLabApiException("GitLab API unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        ServerWebExchange exchange = createMockExchange("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleGitLabApiException(exception, exchange);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("GitLab API unavailable");
        assertThat(body.getErrorCode()).isEqualTo("GITLAB_API_ERROR");
        assertThat(body.getStatus()).isEqualTo(503);
        assertThat(body.getPath()).isEqualTo("/api/test");
    }

    @Test
    void handleGitLabApiException_shouldReturnInternalServerErrorWhenNoHttpStatus() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        GitLabApiException exception = new GitLabApiException("Generic GitLab error");
        ServerWebExchange exchange = createMockExchange("/api/check-mr");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleGitLabApiException(exception, exchange);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Generic GitLab error");
        assertThat(body.getErrorCode()).isEqualTo("GITLAB_API_ERROR");
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getPath()).isEqualTo("/api/check-mr");
    }

    @Test
    void handleLLMApiException_shouldReturnCorrectResponse() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        LLMApiException exception = new LLMApiException("LLM API timeout", HttpStatus.REQUEST_TIMEOUT);
        ServerWebExchange exchange = createMockExchange("/api/check-mr");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleLLMApiException(exception, exchange);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.REQUEST_TIMEOUT);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("LLM API timeout");
        assertThat(body.getErrorCode()).isEqualTo("LLM_API_ERROR");
        assertThat(body.getStatus()).isEqualTo(408);
        assertThat(body.getPath()).isEqualTo("/api/check-mr");
    }

    @Test
    void handleMRCheckException_shouldReturnCorrectResponse() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        MRCheckException exception = new MRCheckException("Validation failed", HttpStatus.BAD_REQUEST);
        ServerWebExchange exchange = createMockExchange("/api/check-mr");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleMRCheckException(exception, exchange);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Validation failed");
        assertThat(body.getErrorCode()).isEqualTo("MR_CHECK_ERROR");
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getPath()).isEqualTo("/api/check-mr");
    }

    @Test
    void handleWebExchangeBindException_shouldReturnBadRequestWithValidationErrors() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        WebExchangeBindException exception = mock(WebExchangeBindException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("checkMRRequest", "projectId", "Project ID cannot be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ServerWebExchange exchange = createMockExchange("/api/check-mr");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleWebExchangeBindException(exception, exchange);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getPath()).isEqualTo("/api/check-mr");
        assertThat(body.getMessage()).contains("Project ID cannot be null");
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        RuntimeException exception = new RuntimeException("Unexpected database error");
        ServerWebExchange exchange = createMockExchange("/api/check-mr");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, exchange);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Unexpected database error");
        assertThat(body.getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getPath()).isEqualTo("/api/check-mr");
    }

    @Test
    void handleGenericException_shouldReturnDefaultMessageWhenNullMessage() {
        // Given
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        RuntimeException exception = new RuntimeException((String) null);
        ServerWebExchange exchange = createMockExchange("/api/health");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, exchange);

        // Then
        assertThat(response).isNotNull();
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Internal server error");
        assertThat(body.getErrorCode()).isEqualTo("INTERNAL_ERROR");
    }
}
