package com.mr.checker.service;

import com.mr.checker.model.AnalysisResult;
import com.mr.checker.model.response.Issue;
import com.mr.checker.model.response.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisFormatterTest {

    private final AnalysisFormatter analysisFormatter = new AnalysisFormatter();

    @Test
    void parseAnalysisResponse_shouldParseValidJsonResponse() {
        // Given
        String llmResponse = """
                {
                  "logical-errors": [
                    {
                      "severity": "HIGH",
                      "description": "Null pointer access in method",
                      "recommendation": "Add null check before using object"
                    }
                  ],
                  "security-vulnerabilities": [
                    {
                      "severity": "HIGH",
                      "description": "SQL injection vulnerability",
                      "recommendation": "Use PreparedStatement instead of string concatenation"
                    }
                  ],
                  "best-practices-violations": [],
                  "performance-issues": [
                    {
                      "severity": "MEDIUM",
                      "description": "Inefficient loop",
                      "recommendation": "Use more efficient data structure"
                    }
                  ]
                }
                """;

        // When
        AnalysisResult result = analysisFormatter.parseAnalysisResponse(llmResponse);

        // Then
        assertThat(result).isNotNull();

        // Проверяем логические ошибки
        assertThat(result.getLogicalErrors()).hasSize(1);
        Issue logicalIssue = result.getLogicalErrors().get(0);
        assertThat(logicalIssue.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(logicalIssue.getDescription()).isEqualTo("Null pointer access in method");
        assertThat(logicalIssue.getRecommendation()).isEqualTo("Add null check before using object");

        // Проверяем security vulnerabilities
        assertThat(result.getSecurityVulnerabilities()).hasSize(1);
        Issue securityIssue = result.getSecurityVulnerabilities().get(0);
        assertThat(securityIssue.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(securityIssue.getDescription()).isEqualTo("SQL injection vulnerability");

        // Проверяем пустые категории
        assertThat(result.getBestPracticesViolations()).isEmpty();

        // Проверяем performance issues
        assertThat(result.getPerformanceIssues()).hasSize(1);
        Issue performanceIssue = result.getPerformanceIssues().get(0);
        assertThat(performanceIssue.getSeverity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void parseAnalysisResponse_shouldHandleEmptyCategories() {
        // Given
        String llmResponse = """
                {
                  "logical-errors": [],
                  "security-vulnerabilities": [],
                  "best-practices-violations": [],
                  "performance-issues": []
                }
                """;

        // When
        AnalysisResult result = analysisFormatter.parseAnalysisResponse(llmResponse);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLogicalErrors()).isEmpty();
        assertThat(result.getSecurityVulnerabilities()).isEmpty();
        assertThat(result.getBestPracticesViolations()).isEmpty();
        assertThat(result.getPerformanceIssues()).isEmpty();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void parseAnalysisResponse_shouldHandleInvalidJson() {
        // Given
        String invalidJson = "This is not JSON at all";

        // When
        AnalysisResult result = analysisFormatter.parseAnalysisResponse(invalidJson);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void parseAnalysisResponse_shouldHandleNullResponse() {
        // When
        AnalysisResult result = analysisFormatter.parseAnalysisResponse(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void extractIssues_shouldParseIssuesFromUnstructuredText() {
        // Given
        String text = """
                I found several issues in this code:

                HIGH: SQL injection vulnerability in UserService.java line 25
                The query uses string concatenation which is dangerous.
                Fix: Use PreparedStatement

                MEDIUM: Null pointer access in method calculate()
                Variable 'result' might be null.
                Recommendation: Add null check

                LOW: Variable naming convention
                Use camelCase for variables
                """;

        // When
        List<Issue> issues = analysisFormatter.extractIssues(text);

        // Then
        assertThat(issues).hasSize(3);

        Issue issue1 = issues.get(0);
        assertThat(issue1.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(issue1.getDescription()).contains("SQL injection vulnerability");

        Issue issue2 = issues.get(1);
        assertThat(issue2.getSeverity()).isEqualTo(Severity.MEDIUM);
        assertThat(issue2.getDescription()).contains("Null pointer access");

        Issue issue3 = issues.get(2);
        assertThat(issue3.getSeverity()).isEqualTo(Severity.LOW);
        assertThat(issue3.getDescription()).contains("Variable naming convention");
    }

    @Test
    void extractIssues_shouldHandleTextWithoutIssues() {
        // Given
        String text = "This is just a regular comment without any issues mentioned.";

        // When
        List<Issue> issues = analysisFormatter.extractIssues(text);

        // Then
        assertThat(issues).isEmpty();
    }

    @Test
    void extractIssues_shouldHandleNullAndEmptyText() {
        // When & Then
        assertThat(analysisFormatter.extractIssues(null)).isEmpty();
        assertThat(analysisFormatter.extractIssues("")).isEmpty();
        assertThat(analysisFormatter.extractIssues("   ")).isEmpty();
    }

    @Test
    void extractIssues_shouldExtractIssuesWithDifferentFormats() {
        // Given
        String text = """
                Issues found:

                HIGH: Potential security breach
                Description: Password stored in plain text
                Recommendation: Use hashing

                MEDIUM: Performance issue
                Slow algorithm detected
                Use more efficient approach

                LOW: Variable naming convention violation
                """;

        // When
        List<Issue> issues = analysisFormatter.extractIssues(text);

        // Then
        assertThat(issues).hasSize(3);
        assertThat(issues.get(0).getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(issues.get(0).getDescription()).contains("Potential security breach");
        assertThat(issues.get(0).getRecommendation()).contains("Use hashing");

        assertThat(issues.get(1).getSeverity()).isEqualTo(Severity.MEDIUM);
        assertThat(issues.get(1).getDescription()).contains("Performance issue");

        assertThat(issues.get(2).getSeverity()).isEqualTo(Severity.LOW);
        assertThat(issues.get(2).getDescription()).contains("Variable naming convention violation");
    }

    @Test
    void categorizeIssues_shouldCategorizeIssuesByKeywords() {
        // Given
        Issue sqlInjection = new Issue();
        sqlInjection.setDescription("SQL injection vulnerability in user login");
        sqlInjection.setSeverity(Severity.HIGH);

        Issue nullPointer = new Issue();
        nullPointer.setDescription("Null pointer exception in calculate method");
        nullPointer.setSeverity(Severity.MEDIUM);

        Issue naming = new Issue();
        naming.setDescription("Variable naming convention violation");
        naming.setSeverity(Severity.LOW);

        Issue performance = new Issue();
        performance.setDescription("Inefficient algorithm with O(n^2) complexity");
        performance.setSeverity(Severity.MEDIUM);

        List<Issue> issues = List.of(sqlInjection, nullPointer, naming, performance);

        // When
        AnalysisResult result = analysisFormatter.categorizeIssues(issues);

        // Then
        assertThat(result).isNotNull();

        // Security issues
        assertThat(result.getSecurityVulnerabilities()).hasSize(1);
        assertThat(result.getSecurityVulnerabilities().get(0).getDescription())
            .isEqualTo("SQL injection vulnerability in user login");

        // Logical errors
        assertThat(result.getLogicalErrors()).hasSize(1);
        assertThat(result.getLogicalErrors().get(0).getDescription())
            .isEqualTo("Null pointer exception in calculate method");

        // Best practices violations
        assertThat(result.getBestPracticesViolations()).hasSize(1);
        assertThat(result.getBestPracticesViolations().get(0).getDescription())
            .isEqualTo("Variable naming convention violation");

        // Performance issues
        assertThat(result.getPerformanceIssues()).hasSize(1);
        assertThat(result.getPerformanceIssues().get(0).getDescription())
            .contains("Inefficient algorithm");
    }

    @Test
    void categorizeIssues_shouldHandleEmptyList() {
        // When
        AnalysisResult result = analysisFormatter.categorizeIssues(List.of());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void categorizeIssues_shouldDetermineSeverityFromIssue() {
        // Given
        Issue issue = new Issue();
        issue.setDescription("Some issue description");
        issue.setSeverity(Severity.HIGH);

        // When
        Severity determinedSeverity = analysisFormatter.determineSeverity(issue);

        // Then
        assertThat(determinedSeverity).isEqualTo(Severity.HIGH);
    }

    @Test
    void categorizeIssues_shouldDetermineSeverityFromContent() {
        // Given
        Issue criticalIssue = new Issue();
        criticalIssue.setDescription("Critical security breach with data exposure");

        Issue normalIssue = new Issue();
        normalIssue.setDescription("Minor code style issue");

        // When
        Severity criticalSeverity = analysisFormatter.determineSeverity(criticalIssue);
        Severity normalSeverity = analysisFormatter.determineSeverity(normalIssue);

        // Then
        assertThat(criticalSeverity).isEqualTo(Severity.HIGH);
        assertThat(normalSeverity).isEqualTo(Severity.LOW);
    }
}
