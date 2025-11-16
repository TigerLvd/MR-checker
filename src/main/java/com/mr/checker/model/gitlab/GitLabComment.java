package com.mr.checker.model.gitlab;

import lombok.Data;

/**
 * Represents a comment to be posted on a GitLab merge request.
 * Used for sending analysis results back to GitLab.
 */
@Data
public class GitLabComment {

    /**
     * The comment body content in Markdown format.
     * Can contain analysis results, recommendations, and formatted code review findings.
     */
    private String body;
}
