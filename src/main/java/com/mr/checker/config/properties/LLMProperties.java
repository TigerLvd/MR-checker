package com.mr.checker.config.properties;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "llm")
@Data
@Validated
public class LLMProperties {

    @NotBlank(message = "LLM URL is required")
    private String url;

    @NotBlank(message = "LLM model is required")
    private String model = "llama2";

    @NotNull(message = "LLM timeout is required")
    private Integer timeout = 120000;

    @NotNull(message = "LLM temperature is required")
    @DecimalMin(value = "0.0", message = "Temperature must be >= 0.0")
    @DecimalMax(value = "2.0", message = "Temperature must be <= 2.0")
    private Double temperature = 0.1;

    @NotNull(message = "LLM max tokens is required")
    private Integer maxTokens = 4096;
}
