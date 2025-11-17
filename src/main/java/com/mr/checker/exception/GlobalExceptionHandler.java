package com.mr.checker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

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
     * @param exchange веб-обмен
     * @return ResponseEntity с ErrorResponse
     */
    @ExceptionHandler(GitLabApiException.class)
    public ResponseEntity<ErrorResponse> handleGitLabApiException(
            GitLabApiException exception,
            ServerWebExchange exchange) {

        String requestPath = getRequestPath(exchange);
        if (isDevelopmentProfile()) {
            log.error("GitLab API error at {}: {}", requestPath, exception.getMessage(), exception);
        } else {
            log.error("GitLab API error: {} (status: {}, path: {})",
                     exception.getMessage(), exception.getHttpStatus(), requestPath);
        }

        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            exception.getErrorCode(),
            exception.getHttpStatus(),
            requestPath
        );

        return new ResponseEntity<>(errorResponse, exception.getHttpStatus());
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
            ServerWebExchange exchange) {

        String requestPath = getRequestPath(exchange);
        if (isDevelopmentProfile()) {
            log.error("LLM API error at {}: {}", requestPath, exception.getMessage(), exception);
        } else {
            log.error("LLM API error: {} (status: {}, path: {})",
                     exception.getMessage(), exception.getHttpStatus(), requestPath);
        }

        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            exception.getErrorCode(),
            exception.getHttpStatus(),
            requestPath
        );

        return new ResponseEntity<>(errorResponse, exception.getHttpStatus());
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
            ServerWebExchange exchange) {

        String requestPath = getRequestPath(exchange);
        if (isDevelopmentProfile()) {
            log.error("MR check error at {}: {}", requestPath, exception.getMessage(), exception);
        } else {
            log.error("MR check error: {} (status: {}, path: {})",
                     exception.getMessage(), exception.getHttpStatus(), requestPath);
        }

        ErrorResponse errorResponse = new ErrorResponse(
            exception.getMessage(),
            exception.getErrorCode(),
            exception.getHttpStatus(),
            requestPath
        );

        return new ResponseEntity<>(errorResponse, exception.getHttpStatus());
    }

    /**
     * Обрабатывает исключения валидации WebExchangeBindException (WebFlux).
     *
     * @param exception исключение валидации
     * @param exchange  веб-обмен
     * @return ResponseEntity с ErrorResponse
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleWebExchangeBindException(
            WebExchangeBindException exception,
            ServerWebExchange exchange) {

        String requestPath = getRequestPath(exchange);
        log.warn("Validation error at {}: {}", requestPath, exception.getMessage());

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
            requestPath
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
            ServerWebExchange exchange) {

        String requestPath = getRequestPath(exchange);
        if (isDevelopmentProfile()) {
            log.error("Unexpected error at {}: {}", requestPath, exception.getMessage(), exception);
        } else {
            log.error("Unexpected error: {} (path: {})", exception.getMessage(), requestPath);
        }

        ErrorResponse errorResponse = new ErrorResponse(
            exception,
            requestPath
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
     * Извлекает путь запроса из ServerWebExchange.
     *
     * @param exchange веб-обмен
     * @return путь запроса или "unknown" если не удалось определить
     */
    private String getRequestPath(ServerWebExchange exchange) {
        try {
            return exchange.getRequest().getPath().value();
        } catch (Exception e) {
            log.warn("Could not extract request path", e);
            return "unknown";
        }
    }
}
