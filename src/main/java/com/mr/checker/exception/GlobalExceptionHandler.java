package com.mr.checker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;

/**
 * Глобальный обработчик исключений для REST API.
 * Конвертирует исключения в унифицированные ErrorResponse.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    /**
     * Обрабатывает исключения GitLabApiException.
     *
     * @param exception исключение GitLab API
     * @param request   веб-запрос
     * @return ResponseEntity с ErrorResponse
     */
    @ExceptionHandler(GitLabApiException.class)
    public ResponseEntity<ErrorResponse> handleGitLabApiException(
            GitLabApiException exception,
            WebRequest request) {

        if (isDevelopmentProfile()) {
            log.error("GitLab API error at {}: {}", getRequestPath(request), exception.getMessage(), exception);
        } else {
            log.error("GitLab API error: {} (status: {}, path: {})",
                     exception.getMessage(), exception.getHttpStatus(), getRequestPath(request));
        }

        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            exception.getErrorCode(),
            exception.getHttpStatus(),
            getRequestPath(request)
        );

        return new ResponseEntity<>(errorResponse, (org.springframework.http.HttpStatusCode) exception.getHttpStatus());
    }

    /**
     * Обрабатывает исключения LLMApiException.
     *
     * @param exception исключение LLM API
     * @param request   веб-запрос
     * @return ResponseEntity с ErrorResponse
     */
    @ExceptionHandler(LLMApiException.class)
    public ResponseEntity<ErrorResponse> handleLLMApiException(
            LLMApiException exception,
            WebRequest request) {

        if (isDevelopmentProfile()) {
            log.error("LLM API error at {}: {}", getRequestPath(request), exception.getMessage(), exception);
        } else {
            log.error("LLM API error: {} (status: {}, path: {})",
                     exception.getMessage(), exception.getHttpStatus(), getRequestPath(request));
        }

        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            exception.getErrorCode(),
            exception.getHttpStatus(),
            getRequestPath(request)
        );

        return new ResponseEntity<>(errorResponse, (org.springframework.http.HttpStatusCode) exception.getHttpStatus());
    }

    /**
     * Обрабатывает исключения MRCheckException.
     *
     * @param exception исключение проверки MR
     * @param request   веб-запрос
     * @return ResponseEntity с ErrorResponse
     */
    @ExceptionHandler(MRCheckException.class)
    public ResponseEntity<ErrorResponse> handleMRCheckException(
            MRCheckException exception,
            WebRequest request) {

        if (isDevelopmentProfile()) {
            log.error("MR check error at {}: {}", getRequestPath(request), exception.getMessage(), exception);
        } else {
            log.error("MR check error: {} (status: {}, path: {})",
                     exception.getMessage(), exception.getHttpStatus(), getRequestPath(request));
        }

        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            exception.getErrorCode(),
            exception.getHttpStatus(),
            getRequestPath(request)
        );

        return new ResponseEntity<>(errorResponse, (org.springframework.http.HttpStatusCode) exception.getHttpStatus());
    }

    /**
     * Обрабатывает исключения валидации MethodArgumentNotValidException.
     *
     * @param exception исключение валидации
     * @param request   веб-запрос
     * @return ResponseEntity с ErrorResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            WebRequest request) {

        log.warn("Validation error at {}: {}", getRequestPath(request), exception.getMessage());

        StringBuilder messageBuilder = new StringBuilder("Validation failed: ");
        exception.getBindingResult().getFieldErrors().forEach(error ->
            messageBuilder.append(error.getField())
                         .append(" - ")
                         .append(error.getDefaultMessage())
                         .append("; ")
        );

        String message = messageBuilder.toString().trim();
        if (message.endsWith(";")) {
            message = message.substring(0, message.length() - 1);
        }

        ErrorResponse errorResponse = new ErrorResponse(
            message,
            "VALIDATION_ERROR",
            HttpStatus.BAD_REQUEST,
            getRequestPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает все остальные исключения (fallback handler).
     *
     * @param exception общее исключение
     * @param request   веб-запрос
     * @return ResponseEntity с ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception,
            WebRequest request) {

        if (isDevelopmentProfile()) {
            log.error("Unexpected error at {}: {}", getRequestPath(request), exception.getMessage(), exception);
        } else {
            log.error("Unexpected error: {} (path: {})", exception.getMessage(), getRequestPath(request));
        }

        ErrorResponse errorResponse = new ErrorResponse(
            exception,
            getRequestPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Проверяет, активен ли профиль разработки.
     *
     * @return true если активен dev профиль, false в противном случае
     */
    private boolean isDevelopmentProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev") ||
               Arrays.asList(environment.getActiveProfiles()).contains("development") ||
               Arrays.asList(environment.getDefaultProfiles()).contains("dev");
    }

    /**
     * Извлекает путь запроса из WebRequest.
     *
     * @param request веб-запрос
     * @return путь запроса или "unknown" если не удалось определить
     */
    private String getRequestPath(WebRequest request) {
        try {
            return ((org.springframework.web.context.request.ServletWebRequest) request)
                .getRequest()
                .getRequestURI();
        } catch (Exception e) {
            log.warn("Could not extract request path", e);
            return "unknown";
        }
    }
}
