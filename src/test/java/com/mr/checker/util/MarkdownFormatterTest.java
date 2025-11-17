package com.mr.checker.util;

import com.mr.checker.model.AnalysisResult;
import com.mr.checker.model.response.CategoryResult;
import com.mr.checker.model.response.Issue;
import com.mr.checker.model.response.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownFormatterTest {

    private final MarkdownFormatter formatter = new MarkdownFormatter();

    @Test
    void formatCategory_shouldFormatCategoryWithIssues() {
        // Given
        Issue issue1 = new Issue();
        issue1.setSeverity(Severity.HIGH);
        issue1.setDescription("SQL injection vulnerability");
        issue1.setRecommendation("Use PreparedStatement");

        Issue issue2 = new Issue();
        issue2.setSeverity(Severity.MEDIUM);
        issue2.setDescription("Null pointer access");
        issue2.setRecommendation("Add null check");

        CategoryResult category = new CategoryResult();
        category.setCategory("security-vulnerabilities");
        category.setIssues(List.of(issue1, issue2));

        // When
        String result = formatter.formatCategory(category);

        // Then
        assertThat(result).contains("## 🔴 Security Vulnerabilities");
        assertThat(result).contains("(2 issues)");
        assertThat(result).contains("### 🚨 HIGH: SQL injection vulnerability");
        assertThat(result).contains("**Recommendation:** Use PreparedStatement");
        assertThat(result).contains("### ⚠️ MEDIUM: Null pointer access");
        assertThat(result).contains("**Recommendation:** Add null check");
    }

    @Test
    void formatCategory_shouldHandleEmptyCategory() {
        // Given
        CategoryResult category = new CategoryResult();
        category.setCategory("logical-errors");
        category.setIssues(List.of());

        // When
        String result = formatter.formatCategory(category);

        // Then
        assertThat(result).contains("## 🧠 Logical Errors");
        assertThat(result).contains("(0 issues)");
        assertThat(result).doesNotContain("###");
    }

    @Test
    void formatCategory_shouldFormatDifferentCategories() {
        // Given
        Issue securityIssue = new Issue();
        securityIssue.setSeverity(Severity.HIGH);
        securityIssue.setDescription("XSS vulnerability");
        securityIssue.setRecommendation("Escape HTML");

        CategoryResult securityCategory = new CategoryResult();
        securityCategory.setCategory("security-vulnerabilities");
        securityCategory.setIssues(List.of(securityIssue));

        Issue performanceIssue = new Issue();
        performanceIssue.setSeverity(Severity.MEDIUM);
        performanceIssue.setDescription("Inefficient loop");
        performanceIssue.setRecommendation("Use more efficient algorithm");

        CategoryResult performanceCategory = new CategoryResult();
        performanceCategory.setCategory("performance-issues");
        performanceCategory.setIssues(List.of(performanceIssue));

        Issue practicesIssue = new Issue();
        practicesIssue.setSeverity(Severity.LOW);
        practicesIssue.setDescription("Poor naming");
        practicesIssue.setRecommendation("Use descriptive names");

        CategoryResult practicesCategory = new CategoryResult();
        practicesCategory.setCategory("best-practices-violations");
        practicesCategory.setIssues(List.of(practicesIssue));

        // When & Then
        String securityResult = formatter.formatCategory(securityCategory);
        assertThat(securityResult).contains("## 🔴 Security Vulnerabilities");

        String performanceResult = formatter.formatCategory(performanceCategory);
        assertThat(performanceResult).contains("## ⚡ Performance Issues");

        String practicesResult = formatter.formatCategory(practicesCategory);
        assertThat(practicesResult).contains("## 📚 Best Practices Violations");
    }

    @Test
    void formatIssue_shouldFormatHighSeverityIssue() {
        // Given
        Issue issue = new Issue();
        issue.setSeverity(Severity.HIGH);
        issue.setDescription("SQL injection vulnerability");
        issue.setRecommendation("Use PreparedStatement instead of string concatenation");

        // When
        String result = formatter.formatIssue(issue);

        // Then
        assertThat(result).isEqualTo("### 🚨 HIGH: SQL injection vulnerability\n**Recommendation:** Use PreparedStatement instead of string concatenation");
    }

    @Test
    void formatIssue_shouldFormatMediumSeverityIssue() {
        // Given
        Issue issue = new Issue();
        issue.setSeverity(Severity.MEDIUM);
        issue.setDescription("Null pointer access");
        issue.setRecommendation("Add null check before accessing object");

        // When
        String result = formatter.formatIssue(issue);

        // Then
        assertThat(result).isEqualTo("### ⚠️ MEDIUM: Null pointer access\n**Recommendation:** Add null check before accessing object");
    }

    @Test
    void formatIssue_shouldFormatLowSeverityIssue() {
        // Given
        Issue issue = new Issue();
        issue.setSeverity(Severity.LOW);
        issue.setDescription("Variable naming convention violation");
        issue.setRecommendation("Use camelCase for variable names");

        // When
        String result = formatter.formatIssue(issue);

        // Then
        assertThat(result).isEqualTo("### ℹ️ LOW: Variable naming convention violation\n**Recommendation:** Use camelCase for variable names");
    }

    @Test
    void formatIssue_shouldHandleMultilineDescription() {
        // Given
        Issue issue = new Issue();
        issue.setSeverity(Severity.HIGH);
        issue.setDescription("Complex security issue\nwith multiple lines");
        issue.setRecommendation("Follow security guidelines");

        // When
        String result = formatter.formatIssue(issue);

        // Then
        assertThat(result).isEqualTo("### 🚨 HIGH: Complex security issue\nwith multiple lines\n**Recommendation:** Follow security guidelines");
    }

    @Test
    void formatIssue_shouldHandleSpecialCharactersInRecommendation() {
        // Given
        Issue issue = new Issue();
        issue.setSeverity(Severity.MEDIUM);
        issue.setDescription("Inefficient algorithm");
        issue.setRecommendation("Use HashMap instead of ArrayList for O(1) lookups");

        // When
        String result = formatter.formatIssue(issue);

        // Then
        assertThat(result).isEqualTo("### ⚠️ MEDIUM: Inefficient algorithm\n**Recommendation:** Use HashMap instead of ArrayList for O(1) lookups");
    }

    @Test
    void formatAnalysisResults_shouldFormatCompleteAnalysisWithAllCategories() {
        // Given
        Issue securityIssue = new Issue();
        securityIssue.setSeverity(Severity.HIGH);
        securityIssue.setDescription("SQL injection vulnerability");
        securityIssue.setRecommendation("Use PreparedStatement");

        Issue performanceIssue = new Issue();
        performanceIssue.setSeverity(Severity.MEDIUM);
        performanceIssue.setDescription("Inefficient loop");
        performanceIssue.setRecommendation("Use more efficient algorithm");

        AnalysisResult analysisResult = new AnalysisResult();
        analysisResult.setSecurityVulnerabilities(List.of(securityIssue));
        analysisResult.setPerformanceIssues(List.of(performanceIssue));
        // logicalErrors and bestPracticesViolations remain null/empty

        // When
        String result = formatter.formatAnalysisResults(analysisResult);

        // Then
        assertThat(result).contains("# 🔍 Code Analysis Results");
        assertThat(result).contains("**Total issues found:** 2");
        assertThat(result).contains("## 🔴 Security Vulnerabilities");
        assertThat(result).contains("(1 issues)");
        assertThat(result).contains("## ⚡ Performance Issues");
        assertThat(result).contains("(1 issues)");
        assertThat(result).contains("## 🧠 Logical Errors");
        assertThat(result).contains("(0 issues)");
        assertThat(result).contains("## 📚 Best Practices Violations");
        assertThat(result).contains("(0 issues)");
        assertThat(result).contains("### 🚨 HIGH: SQL injection vulnerability");
        assertThat(result).contains("### ⚠️ MEDIUM: Inefficient loop");
    }

    @Test
    void formatAnalysisResults_shouldHandleEmptyAnalysisResult() {
        // Given
        AnalysisResult analysisResult = new AnalysisResult();

        // When
        String result = formatter.formatAnalysisResults(analysisResult);

        // Then
        assertThat(result).contains("# 🔍 Code Analysis Results");
        assertThat(result).contains("**Total issues found:** 0");
        assertThat(result).contains("## 🔴 Security Vulnerabilities\n(0 issues)");
        assertThat(result).contains("## 🧠 Logical Errors\n(0 issues)");
        assertThat(result).contains("## ⚡ Performance Issues\n(0 issues)");
        assertThat(result).contains("## 📚 Best Practices Violations\n(0 issues)");
    }

    @Test
    void formatAnalysisResults_shouldFormatMultipleIssuesInCategories() {
        // Given
        Issue securityIssue1 = new Issue();
        securityIssue1.setSeverity(Severity.HIGH);
        securityIssue1.setDescription("SQL injection");
        securityIssue1.setRecommendation("Use PreparedStatement");

        Issue securityIssue2 = new Issue();
        securityIssue2.setSeverity(Severity.MEDIUM);
        securityIssue2.setDescription("XSS vulnerability");
        securityIssue2.setRecommendation("Escape HTML");

        Issue logicalIssue = new Issue();
        logicalIssue.setSeverity(Severity.LOW);
        logicalIssue.setDescription("Dead code");
        logicalIssue.setRecommendation("Remove unused code");

        AnalysisResult analysisResult = new AnalysisResult();
        analysisResult.setSecurityVulnerabilities(List.of(securityIssue1, securityIssue2));
        analysisResult.setLogicalErrors(List.of(logicalIssue));

        // When
        String result = formatter.formatAnalysisResults(analysisResult);

        // Then
        assertThat(result).contains("**Total issues found:** 3");
        assertThat(result).contains("## 🔴 Security Vulnerabilities\n(2 issues)");
        assertThat(result).contains("## 🧠 Logical Errors\n(1 issues)");
        assertThat(result).contains("### 🚨 HIGH: SQL injection");
        assertThat(result).contains("### ⚠️ MEDIUM: XSS vulnerability");
        assertThat(result).contains("### ℹ️ LOW: Dead code");
    }

    @Test
    void formatAnalysisResults_shouldIncludeTimestamp() {
        // Given
        AnalysisResult analysisResult = new AnalysisResult();

        // When
        String result = formatter.formatAnalysisResults(analysisResult);

        // Then
        assertThat(result).contains("---");
        assertThat(result).contains("Analysis completed at:");
    }
}
