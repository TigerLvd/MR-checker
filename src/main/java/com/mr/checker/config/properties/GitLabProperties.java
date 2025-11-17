package com.mr.checker.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "gitlab")
@Data
@Validated
public class GitLabProperties {

    @NotBlank(message = "GitLab URL is required")
    private String url;

    @NotBlank(message = "GitLab token is required")
    private String token;

    @NotBlank(message = "GitLab API version is required")
    private String apiVersion = "v4";

    @NotNull(message = "GitLab timeout is required")
    private Integer timeout = 30000;
}
