package com.mr.checker.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response model for MR checking operation.
 * Contains the analysis results, status, and metadata about the check.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckMRResponse {

    /**
     * Status of the analysis (success, warning, failure).
     */
    private String status;

    /**
     * Human-readable summary of the analysis results.
     */
    private String summary;

    /**
     * Detailed results grouped by categories (security, performance, etc.).
     */
    private List<CategoryResult> details;

    /**
     * Timestamp when the analysis was performed.
     */
    private LocalDateTime checkedAt;
}
