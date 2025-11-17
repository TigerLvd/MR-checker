package com.mr.checker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

/**
 * Исключение, возникающее при ошибках взаимодействия с LLM API.
 * Содержит информацию о HTTP статусе, коде ошибки и времени возникновения.
 */
@Getter
public class LLMApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final Instant timestamp;

    /**
     * Создает исключение с сообщением об ошибке.
     * HTTP статус по умолчанию: INTERNAL_SERVER_ERROR.
     *
     * @param message сообщение об ошибке
     */
    public LLMApiException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "LLM_API_ERROR";
        this.timestamp = Instant.now();
    }

    /**
     * Создает исключение с сообщением об ошибке и причиной.
     * HTTP статус по умолчанию: INTERNAL_SERVER_ERROR.
     *
     * @param message сообщение об ошибке
     * @param cause   причина возникновения ошибки
     */
    public LLMApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "LLM_API_ERROR";
        this.timestamp = Instant.now();
    }

    /**
     * Создает исключение с сообщением об ошибке и HTTP статусом.
     *
     * @param message    сообщение об ошибке
     * @param httpStatus HTTP статус ошибки
     */
    public LLMApiException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = "LLM_API_ERROR";
        this.timestamp = Instant.now();
    }

    /**
     * Создает исключение с сообщением об ошибке, причиной и HTTP статусом.
     *
     * @param message    сообщение об ошибке
     * @param cause      причина возникновения ошибки
     * @param httpStatus HTTP статус ошибки
     */
    public LLMApiException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = "LLM_API_ERROR";
        this.timestamp = Instant.now();
    }

    /**
     * Создает исключение с полными параметрами.
     *
     * @param message    сообщение об ошибке
     * @param cause      причина возникновения ошибки
     * @param httpStatus HTTP статус ошибки
     * @param errorCode  код ошибки
     */
    public LLMApiException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
    }
}
