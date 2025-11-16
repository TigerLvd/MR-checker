package com.mr.checker.model.response;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Represents the result of analysis for a specific category of issues.
 * Groups together issues of the same type (security, performance, etc.) and provides summary information.
 */
@Data
public class CategoryResult {

    /**
     * Category name (e.g., "security", "performance", "best-practices").
     */
    private String category;

    /**
     * List of issues found in this category.
     */
    private List<Issue> issues;

    /**
     * Returns the number of issues in this category.
     * Automatically calculated based on the issues list size.
     *
     * @return number of issues, 0 if issues list is null or empty
     */
    public Integer getIssuesCount() {
        return issues != null ? issues.size() : 0;
    }

    /**
     * Returns an unmodifiable view of the issues list.
     * Prevents external modification of the internal list.
     *
     * @return unmodifiable list of issues, empty list if issues is null
     */
    public List<Issue> getIssues() {
        return issues != null ? Collections.unmodifiableList(issues) : Collections.emptyList();
    }

    /**
     * Checks if this category result has any issues.
     *
     * @return true if there are issues, false otherwise
     */
    public boolean hasIssues() {
        return getIssuesCount() > 0;
    }
}
