package com.mr.checker.model.response;

import lombok.Data;

/**
 * Represents a single issue found during code analysis.
 * Contains severity level, description of the problem, and recommendation for fixing it.
 */
@Data
public class Issue {

    /**
     * Severity level of the issue.
     */
    private Severity severity;

    /**
     * Description of the issue found in the code.
     */
    private String description;

    /**
     * Recommendation on how to fix the issue.
     */
    private String recommendation;
}
