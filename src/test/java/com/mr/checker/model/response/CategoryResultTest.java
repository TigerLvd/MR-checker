package com.mr.checker.model.response;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryResultTest {

    @Test
    void shouldCreateCategoryResultWithIssues() {
        // Given
        Issue issue1 = new Issue();
        issue1.setSeverity(Severity.HIGH);
        issue1.setDescription("Security vulnerability");

        Issue issue2 = new Issue();
        issue2.setSeverity(Severity.MEDIUM);
        issue2.setDescription("Code style issue");

        List<Issue> issues = Arrays.asList(issue1, issue2);

        // When
        CategoryResult categoryResult = new CategoryResult();
        categoryResult.setCategory("security");
        categoryResult.setIssues(issues);

        // Then
        assertThat(categoryResult.getCategory()).isEqualTo("security");
        assertThat(categoryResult.getIssues()).hasSize(2);
        assertThat(categoryResult.getIssuesCount()).isEqualTo(2);
    }

    @Test
    void shouldCalculateIssuesCountAutomatically() {
        // Given
        CategoryResult categoryResult = new CategoryResult();
        categoryResult.setCategory("performance");

        Issue issue1 = new Issue();
        issue1.setSeverity(Severity.LOW);

        Issue issue2 = new Issue();
        issue2.setSeverity(Severity.MEDIUM);

        Issue issue3 = new Issue();
        issue3.setSeverity(Severity.HIGH);

        // When
        categoryResult.setIssues(Arrays.asList(issue1, issue2, issue3));

        // Then
        assertThat(categoryResult.getIssuesCount()).isEqualTo(3);
    }

    @Test
    void shouldHandleEmptyIssuesList() {
        // Given
        CategoryResult categoryResult = new CategoryResult();
        categoryResult.setCategory("best-practices");
        categoryResult.setIssues(Arrays.asList());

        // Then
        assertThat(categoryResult.getIssues()).isEmpty();
        assertThat(categoryResult.getIssuesCount()).isEqualTo(0);
        assertThat(categoryResult.hasIssues()).isFalse();
    }

    @Test
    void shouldReturnHasIssuesTrueWhenIssuesExist() {
        // Given
        CategoryResult categoryResult = new CategoryResult();
        categoryResult.setCategory("security");

        Issue issue = new Issue();
        issue.setSeverity(Severity.HIGH);
        issue.setDescription("Security issue");

        categoryResult.setIssues(Arrays.asList(issue));

        // Then
        assertThat(categoryResult.hasIssues()).isTrue();
        assertThat(categoryResult.getIssuesCount()).isEqualTo(1);
    }
}
