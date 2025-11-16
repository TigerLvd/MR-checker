package com.mr.checker.service;

import com.mr.checker.model.AnalysisResult;
import com.mr.checker.model.gitlab.MRChanges;
import com.mr.checker.model.request.CheckMRRequest;
import com.mr.checker.model.response.CheckMRResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MRCheckerService {

    private final GitLabService gitLabService;
    private final LLMAnalysisService llmAnalysisService;

    public Mono<CheckMRResponse> checkMR(CheckMRRequest request) {
        log.info("Starting MR check for projectId={}, mrIid={}", request.getProjectId(), request.getMrIid());

        return gitLabService.getMRChanges(request.getProjectId(), request.getMrIid())
                .flatMap(mrChanges -> analyzeMRChanges(mrChanges))
                .map(analysisResult -> buildResponse(analysisResult))
                .doOnNext(response -> log.info("Successfully completed MR check with status: {}", response.getStatus()))
                .doOnError(error -> log.error("Failed to check MR", error));
    }

    private Mono<AnalysisResult> analyzeMRChanges(MRChanges mrChanges) {
        if (mrChanges.getChanges() == null || mrChanges.getChanges().isEmpty()) {
            log.warn("No changes found in MR");
            return Mono.just(new AnalysisResult());
        }

        String codeSnippet = extractCodeFromChanges(mrChanges);
        return llmAnalysisService.analyzeCode(codeSnippet);
    }

    private String extractCodeFromChanges(MRChanges mrChanges) {
        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append("Merge Request: ").append(mrChanges.getTitle()).append("\n");
        codeBuilder.append("Description: ").append(mrChanges.getDescription()).append("\n\n");

        mrChanges.getChanges().forEach(diffFile -> {
            codeBuilder.append("File: ").append(diffFile.getNewPath() != null ? diffFile.getNewPath() : diffFile.getOldPath()).append("\n");
            if (diffFile.getDiff() != null) {
                codeBuilder.append(diffFile.getDiff()).append("\n\n");
            }
        });

        return codeBuilder.toString();
    }

    private CheckMRResponse buildResponse(AnalysisResult analysisResult) {
        String status = analysisResult.hasIssues() ? "warning" : "success";
        String summary = analysisResult.hasIssues()
                ? String.format("Found %d issues in the code", analysisResult.getTotalIssuesCount())
                : "No issues found in the code";

        return CheckMRResponse.builder()
                .status(status)
                .summary(summary)
                .details(analysisResult.getCategoryResults())
                .checkedAt(LocalDateTime.now())
                .build();
    }
}