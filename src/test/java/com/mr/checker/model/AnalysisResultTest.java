package com.mr.checker.model;

import com.mr.checker.model.response.CategoryResult;
import com.mr.checker.model.response.Issue;
import com.mr.checker.model.response.Severity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisResultTest {

    @Test
    void shouldStoreIssuesByCategories() {
        // Given
        Issue logicalError = new Issue();
        logicalError.setSeverity(Severity.HIGH);
        logicalError.setDescription("Null pointer exception risk");

        Issue securityIssue = new Issue();
        securityIssue.setSeverity(Severity.CRITICAL);
        securityIssue.setDescription("SQL injection vulnerability");

        Issue performanceIssue = new Issue();
        performanceIssue.setSeverity(Severity.MEDIUM);
        performanceIssue.setDescription("Inefficient algorithm");

        Issue bestPracticeViolation = new Issue();
        bestPracticeViolation.setSeverity(Severity.LOW);
        bestPracticeViolation.setDescription("Missing documentation");

        // When
        AnalysisResult result = new AnalysisResult();
        result.setLogicalErrors(Arrays.asList(logicalError));
        result.setSecurityVulnerabilities(Arrays.asList(securityIssue));
        result.setPerformanceIssues(Arrays.asList(performanceIssue));
        result.setBestPracticesViolations(Arrays.asList(bestPracticeViolation));

        // Then
        assertThat(result.getLogicalErrors()).hasSize(1);
        assertThat(result.getSecurityVulnerabilities()).hasSize(1);
        assertThat(result.getPerformanceIssues()).hasSize(1);
        assertThat(result.getBestPracticesViolations()).hasSize(1);
    }

    @Test
    void shouldCalculateTotalIssuesCount() {
        // Given
        Issue issue1 = new Issue();
        Issue issue2 = new Issue();
        Issue issue3 = new Issue();

        AnalysisResult result = new AnalysisResult();
        result.setLogicalErrors(Arrays.asList(issue1));
        result.setSecurityVulnerabilities(Arrays.asList(issue2));
        result.setPerformanceIssues(Arrays.asList(issue3));

        // When & Then
        assertThat(result.getTotalIssuesCount()).isEqualTo(3);
    }

    @Test
    void shouldReturnCategoryResults() {
        // Given
        Issue logicalIssue = new Issue();
        logicalIssue.setSeverity(Severity.HIGH);

        Issue securityIssue = new Issue();
        securityIssue.setSeverity(Severity.CRITICAL);

        AnalysisResult result = new AnalysisResult();
        result.setLogicalErrors(Arrays.asList(logicalIssue));
        result.setSecurityVulnerabilities(Arrays.asList(securityIssue));

        // When
        List<CategoryResult> categoryResults = result.getCategoryResults();

        // Then
        assertThat(categoryResults).hasSize(4); // All categories should be present

        CategoryResult logicalCategory = categoryResults.stream()
                .filter(cr -> "logical".equals(cr.getCategory()))
                .findFirst().orElse(null);
        assertThat(logicalCategory).isNotNull();
        assertThat(logicalCategory.getIssuesCount()).isEqualTo(1);

        CategoryResult securityCategory = categoryResults.stream()
                .filter(cr -> "security".equals(cr.getCategory()))
                .findFirst().orElse(null);
        assertThat(securityCategory).isNotNull();
        assertThat(securityCategory.getIssuesCount()).isEqualTo(1);

        // Empty categories should still be present
        CategoryResult bestPracticesCategory = categoryResults.stream()
                .filter(cr -> "best-practices".equals(cr.getCategory()))
                .findFirst().orElse(null);
        assertThat(bestPracticesCategory).isNotNull();
        assertThat(bestPracticesCategory.getIssuesCount()).isEqualTo(0);

        CategoryResult performanceCategory = categoryResults.stream()
                .filter(cr -> "performance".equals(cr.getCategory()))
                .findFirst().orElse(null);
        assertThat(performanceCategory).isNotNull();
        assertThat(performanceCategory.getIssuesCount()).isEqualTo(0);
    }

    @Test
    void shouldReturnEmptyCategoriesWhenNoIssues() {
        // Given
        AnalysisResult result = new AnalysisResult();

        // When
        List<CategoryResult> categoryResults = result.getCategoryResults();
        int totalCount = result.getTotalIssuesCount();

        // Then
        assertThat(categoryResults).hasSize(4); // All categories should be present
        assertThat(totalCount).isEqualTo(0);

        // All categories should have 0 issues
        categoryResults.forEach(category ->
                assertThat(category.getIssuesCount()).isEqualTo(0));
    }

    @Test
    void shouldReturnTrueWhenHasIssues() {
        // Given
        AnalysisResult result = new AnalysisResult();
        result.setLogicalErrors(Arrays.asList(new Issue()));

        // When & Then
        assertThat(result.hasIssues()).isTrue();
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenEmpty() {
        // Given
        AnalysisResult result = new AnalysisResult();

        // When & Then
        assertThat(result.hasIssues()).isFalse();
        assertThat(result.isEmpty()).isTrue();
    }
}
