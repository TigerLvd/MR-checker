package com.mr.checker.model.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CheckMRResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldCreateCompleteResponse() {
        // Given
        Issue issue1 = new Issue();
        issue1.setSeverity(Severity.HIGH);
        issue1.setDescription("Security vulnerability");
        issue1.setRecommendation("Use prepared statements");

        Issue issue2 = new Issue();
        issue2.setSeverity(Severity.MEDIUM);
        issue2.setDescription("Performance issue");
        issue2.setRecommendation("Optimize query");

        CategoryResult securityResult = new CategoryResult();
        securityResult.setCategory("security");
        securityResult.setIssues(Arrays.asList(issue1));

        CategoryResult performanceResult = new CategoryResult();
        performanceResult.setCategory("performance");
        performanceResult.setIssues(Arrays.asList(issue2));

        CheckMRResponse response = CheckMRResponse.builder()
                .status("warning")
                .summary("Found 2 issues in 2 categories")
                .details(Arrays.asList(securityResult, performanceResult))
                .checkedAt(LocalDateTime.of(2025, 11, 16, 16, 30))
                .build();

        // Then
        assertThat(response.getStatus()).isEqualTo("warning");
        assertThat(response.getSummary()).isEqualTo("Found 2 issues in 2 categories");
        assertThat(response.getDetails()).hasSize(2);
        assertThat(response.getCheckedAt()).isEqualTo(LocalDateTime.of(2025, 11, 16, 16, 30));
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        CheckMRResponse response = CheckMRResponse.builder()
                .status("success")
                .summary("No issues found")
                .details(Arrays.asList())
                .checkedAt(LocalDateTime.of(2025, 11, 16, 16, 30))
                .build();

        // When
        String json = objectMapper.writeValueAsString(response);

        // Then
        assertThat(json).contains("\"status\":\"success\"");
        assertThat(json).contains("\"summary\":\"No issues found\"");
        assertThat(json).contains("\"details\":[]");
        assertThat(json).contains("\"checkedAt\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        // Given
        String json = "{\"status\":\"warning\",\"summary\":\"Found issues\",\"details\":[],\"checkedAt\":\"2025-11-16T16:30:00\"}";

        // When
        CheckMRResponse response = objectMapper.readValue(json, CheckMRResponse.class);

        // Then
        assertThat(response.getStatus()).isEqualTo("warning");
        assertThat(response.getSummary()).isEqualTo("Found issues");
        assertThat(response.getDetails()).isEmpty();
        assertThat(response.getCheckedAt()).isEqualTo(LocalDateTime.of(2025, 11, 16, 16, 30));
    }
}
