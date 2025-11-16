package com.mr.checker.model.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IssueTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateIssueWithAllFields() {
        // Given
        Issue issue = new Issue();
        issue.setSeverity(Severity.HIGH);
        issue.setDescription("Critical security vulnerability found");
        issue.setRecommendation("Use prepared statements to prevent SQL injection");

        // Then
        assertThat(issue.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(issue.getDescription()).isEqualTo("Critical security vulnerability found");
        assertThat(issue.getRecommendation()).isEqualTo("Use prepared statements to prevent SQL injection");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        Issue issue = new Issue();
        issue.setSeverity(Severity.MEDIUM);
        issue.setDescription("Code style violation");
        issue.setRecommendation("Follow Java naming conventions");

        // When
        String json = objectMapper.writeValueAsString(issue);

        // Then
        assertThat(json).contains("\"severity\":\"MEDIUM\"");
        assertThat(json).contains("\"description\":\"Code style violation\"");
        assertThat(json).contains("\"recommendation\":\"Follow Java naming conventions\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        // Given
        String json = "{\"severity\":\"LOW\",\"description\":\"Minor optimization\",\"recommendation\":\"Consider using StringBuilder\"}";

        // When
        Issue issue = objectMapper.readValue(json, Issue.class);

        // Then
        assertThat(issue.getSeverity()).isEqualTo(Severity.LOW);
        assertThat(issue.getDescription()).isEqualTo("Minor optimization");
        assertThat(issue.getRecommendation()).isEqualTo("Consider using StringBuilder");
    }
}
