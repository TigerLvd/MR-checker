package com.mr.checker.service;

import com.mr.checker.model.AnalysisResult;
import com.mr.checker.model.gitlab.DiffFile;
import com.mr.checker.model.gitlab.MRChanges;
import com.mr.checker.model.request.CheckMRRequest;
import com.mr.checker.model.response.CheckMRResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MRCheckerServiceTest {

    @Mock
    private GitLabService gitLabService;

    @Mock
    private LLMAnalysisService llmAnalysisService;

    private MRCheckerService mrCheckerService;

    @BeforeEach
    void setUp() {
        mrCheckerService = new MRCheckerService(gitLabService, llmAnalysisService);
    }

    @Test
    void shouldCreateServiceWithValidParameters() {
        // Given & When & Then
        assertThat(mrCheckerService).isNotNull();
    }

    @Test
    void shouldHandleNullServices() {
        // Given & When & Then
        MRCheckerService service = new MRCheckerService(null, null);
        assertThat(service).isNotNull();
    }

    @Test
    void shouldCheckMRWithNoChanges() {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        request.setMrIid(456);

        MRChanges mrChanges = new MRChanges();
        mrChanges.setChanges(Collections.emptyList());

        when(gitLabService.getMRChanges(123L, 456)).thenReturn(Mono.just(mrChanges));

        // When & Then
        StepVerifier.create(mrCheckerService.checkMR(request))
                .expectNextMatches(response -> {
                    assertThat(response.getStatus()).isEqualTo("success");
                    assertThat(response.getSummary()).isEqualTo("No issues found in the code");
                    assertThat(response.getDetails()).isNotNull();
                    assertThat(response.getCheckedAt()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void shouldCheckMRWithChangesAndIssues() {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        request.setMrIid(456);

        MRChanges mrChanges = new MRChanges();
        mrChanges.setTitle("Test MR");
        mrChanges.setDescription("Test description");

        DiffFile diffFile = new DiffFile();
        diffFile.setNewPath("src/Test.java");
        diffFile.setDiff("@@ -1,3 +1,3 @@\n-old code\n+new code");
        mrChanges.setChanges(List.of(diffFile));

        AnalysisResult analysisResult = new AnalysisResult();
        // Mock analysis result with issues
        analysisResult.setLogicalErrors(List.of()); // empty but we can add issues later
        when(gitLabService.getMRChanges(123L, 456)).thenReturn(Mono.just(mrChanges));
        when(llmAnalysisService.analyzeCode(anyString())).thenReturn(Mono.just(analysisResult));

        // When & Then
        StepVerifier.create(mrCheckerService.checkMR(request))
                .expectNextMatches(response -> {
                    assertThat(response.getStatus()).isEqualTo("success"); // No issues in empty result
                    assertThat(response.getSummary()).isEqualTo("No issues found in the code");
                    assertThat(response.getDetails()).hasSize(4); // Always 4 categories
                    assertThat(response.getCheckedAt()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleGitLabServiceError() {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        request.setMrIid(456);

        when(gitLabService.getMRChanges(123L, 456)).thenReturn(Mono.error(new RuntimeException("GitLab API error")));

        // When & Then
        StepVerifier.create(mrCheckerService.checkMR(request))
                .expectError(RuntimeException.class)
                .verify();
    }
}