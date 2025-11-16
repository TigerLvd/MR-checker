package com.mr.checker.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request model for MR checking operation.
 * Contains the necessary information to identify and access a GitLab Merge Request.
 */
@Data
public class CheckMRRequest {

    /**
     * GitLab project ID where the merge request is located.
     */
    @NotNull(message = "Project ID is required")
    private Long projectId;

    /**
     * Merge request IID (internal ID within the project).
     */
    @NotNull(message = "MR IID is required")
    private Integer mrIid;

    /**
     * GitLab access token for API authentication.
     * If not provided, will use the token from configuration.
     */
    private String gitlabToken;
}
