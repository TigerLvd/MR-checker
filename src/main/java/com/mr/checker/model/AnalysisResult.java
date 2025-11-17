package com.mr.checker.model;

import com.mr.checker.model.response.CategoryResult;
import com.mr.checker.model.response.Issue;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the complete result of code analysis performed by LLM.
 * Contains issues categorized by type: logical errors, security vulnerabilities,
 * best practices violations, and performance issues.
 */
@Data
public class AnalysisResult {

    /**
     * List of logical errors found in the code (e.g., null pointer exceptions, incorrect logic).
     */
    private List<Issue> logicalErrors;

    /**
     * List of security vulnerabilities found in the code (e.g., SQL injection, XSS).
     */
    private List<Issue> securityVulnerabilities;

    /**
     * List of best practices violations found in the code (e.g., naming conventions, code style).
     */
    private List<Issue> bestPracticesViolations;

    /**
     * List of performance issues found in the code (e.g., inefficient algorithms, memory leaks).
     */
    private List<Issue> performanceIssues;

    /**
     * Returns the total number of issues found across all categories.
     *
     * @return total count of all issues
     */
    public Integer getTotalIssuesCount() {
        return getLogicalErrorsCount() + getSecurityVulnerabilitiesCount() +
               getBestPracticesViolationsCount() + getPerformanceIssuesCount();
    }

    /**
     * Returns a list of CategoryResult objects for all issue categories.
     * Always returns all 4 categories (logical, security, best-practices, performance),
     * even if some categories have no issues.
     *
     * @return list of CategoryResult objects for all categories
     */
    public List<CategoryResult> getCategoryResults() {
        List<CategoryResult> results = new ArrayList<>();

        // Always include all categories, even if empty
        CategoryResult logicalCategory = new CategoryResult();
        logicalCategory.setCategory("logical");
        logicalCategory.setIssues(logicalErrors != null ? logicalErrors : Collections.emptyList());
        results.add(logicalCategory);

        CategoryResult securityCategory = new CategoryResult();
        securityCategory.setCategory("security");
        securityCategory.setIssues(securityVulnerabilities != null ? securityVulnerabilities : Collections.emptyList());
        results.add(securityCategory);

        CategoryResult bestPracticesCategory = new CategoryResult();
        bestPracticesCategory.setCategory("best-practices");
        bestPracticesCategory.setIssues(bestPracticesViolations != null ? bestPracticesViolations : Collections.emptyList());
        results.add(bestPracticesCategory);

        CategoryResult performanceCategory = new CategoryResult();
        performanceCategory.setCategory("performance");
        performanceCategory.setIssues(performanceIssues != null ? performanceIssues : Collections.emptyList());
        results.add(performanceCategory);

        return results;
    }

    /**
     * Checks if this analysis result contains no issues.
     *
     * @return true if no issues were found, false otherwise
     */
    public boolean isEmpty() {
        return getTotalIssuesCount() == 0;
    }

    /**
     * Checks if this analysis result contains any issues.
     *
     * @return true if issues were found, false otherwise
     */
    public boolean hasIssues() {
        return !isEmpty();
    }

    private Integer getLogicalErrorsCount() {
        return logicalErrors != null ? logicalErrors.size() : 0;
    }

    private Integer getSecurityVulnerabilitiesCount() {
        return securityVulnerabilities != null ? securityVulnerabilities.size() : 0;
    }

    private Integer getBestPracticesViolationsCount() {
        return bestPracticesViolations != null ? bestPracticesViolations.size() : 0;
    }

    private Integer getPerformanceIssuesCount() {
        return performanceIssues != null ? performanceIssues.size() : 0;
    }
}
