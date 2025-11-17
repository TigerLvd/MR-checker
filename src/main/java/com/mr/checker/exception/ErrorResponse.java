package com.mr.checker.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

/**
 * DTO для унифицированного ответа об ошибках API.
 * Содержит информацию об ошибке в структурированном виде.
 */
@Getter
public class ErrorResponse {

    private final String message;
    private final String errorCode;
    private final int status;
    private final String path;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private final Instant timestamp;

    /**
     * Создает ErrorResponse с полными параметрами.
     *
     * @param message   сообщение об ошибке
     * @param errorCode код ошибки
     * @param status    HTTP статус
     * @param path      путь запроса, где произошла ошибка
     */
    public ErrorResponse(String message, String errorCode, HttpStatus status, String path) {
        this.message = message;
        this.errorCode = errorCode;
        this.status = status.value();
        this.path = path;
        this.timestamp = Instant.now();
    }

    /**
     * Создает ErrorResponse на основе кастомного исключения.
     *
     * @param exception исключение, содержащее информацию об ошибке
     * @param path      путь запроса, где произошла ошибка
     */
    public ErrorResponse(MRCheckException exception, String path) {
        this.message = exception.getMessage();
        this.errorCode = exception.getErrorCode();
        this.status = exception.getHttpStatus().value();
        this.path = path;
        this.timestamp = exception.getTimestamp();
    }

    /**
     * Создает ErrorResponse на основе общего исключения.
     *
     * @param exception общее исключение
     * @param path      путь запроса, где произошла ошибка
     */
    public ErrorResponse(Exception exception, String path) {
        if (exception instanceof MRCheckException mrCheckException) {
            // Если это кастомное исключение, используем его данные
            this.message = mrCheckException.getMessage();
            this.errorCode = mrCheckException.getErrorCode();
            this.status = mrCheckException.getHttpStatus().value();
        } else {
            // Для общих исключений
            this.message = exception.getMessage() != null ? exception.getMessage() : "Internal server error";
            this.errorCode = "INTERNAL_ERROR";
            this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        this.path = path;
        this.timestamp = Instant.now();
    }
}
