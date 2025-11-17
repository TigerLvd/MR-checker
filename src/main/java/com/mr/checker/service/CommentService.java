package com.mr.checker.service;

import com.mr.checker.client.GitLabApiClient;
import com.mr.checker.model.AnalysisResult;
import com.mr.checker.util.MarkdownFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис для создания комментариев в GitLab MR.
 * Форматирует результаты анализа и публикует их как комментарии.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final GitLabApiClient gitLabApiClient;
    private final MarkdownFormatter markdownFormatter;

    /**
     * Создает и публикует комментарий с результатами анализа кода.
     * Комментарий публикуется только если найдены проблемы.
     *
     * @param projectId ID проекта GitLab
     * @param mrIid IID merge request
     * @param analysisResult результаты анализа кода
     */
    public void postAnalysisComment(Long projectId, Long mrIid, AnalysisResult analysisResult) {
        if (!shouldPostComment(analysisResult)) {
            log.info("No issues found in analysis result, skipping comment creation for MR {}/{}", projectId, mrIid);
            return;
        }

        try {
            String comment = markdownFormatter.formatAnalysisResults(analysisResult);
            gitLabApiClient.postComment(projectId, mrIid, comment);

            log.info("Successfully posted analysis comment to MR {}/{} with {} issues",
                    projectId, mrIid, analysisResult.getTotalIssuesCount());
        } catch (Exception e) {
            log.error("Failed to post analysis comment to MR {}/{}: {}", projectId, mrIid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Создает и публикует комментарий об ошибке анализа кода.
     * Создает информативное сообщение в зависимости от типа ошибки.
     *
     * @param projectId ID проекта GitLab
     * @param mrIid IID merge request
     * @param errorMessage сообщение об ошибке
     */
    public void postErrorComment(Long projectId, Long mrIid, String errorMessage) {
        try {
            String comment = formatErrorComment(errorMessage);
            gitLabApiClient.postComment(projectId, mrIid, comment);

            log.warn("Posted error comment to MR {}/{}: {}", projectId, mrIid, errorMessage);
        } catch (Exception e) {
            log.error("Failed to post error comment to MR {}/{}: {}", projectId, mrIid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Форматирует сообщение об ошибке в Markdown комментарий.
     *
     * @param errorMessage сообщение об ошибке
     * @return отформатированный комментарий
     */
    private String formatErrorComment(String errorMessage) {
        String safeErrorMessage = errorMessage != null ? errorMessage : "Unknown error occurred";

        // Определяем тип ошибки и соответствующее сообщение
        String advice = getErrorAdvice(safeErrorMessage);

        return "## ❌ Analysis Error\n\n" +
               "Code analysis could not be completed due to the following error:\n\n" +
               "**Error:** " + safeErrorMessage + "\n\n" +
               advice;
    }

    /**
     * Возвращает совет по устранению ошибки в зависимости от её типа.
     *
     * @param errorMessage сообщение об ошибке
     * @return совет по устранению
     */
    private String getErrorAdvice(String errorMessage) {
        String lowerError = errorMessage.toLowerCase();

        if (lowerError.contains("gitlab") || lowerError.contains("authentication") ||
            lowerError.contains("token") || lowerError.contains("unauthorized")) {
            return "Please check the GitLab token configuration or contact the development team.";
        }

        if (lowerError.contains("llm") || lowerError.contains("timeout") ||
            lowerError.contains("service unavailable") || lowerError.contains("overload")) {
            return "Please try again later. The analysis service may be temporarily overloaded.";
        }

        if (lowerError.contains("network") || lowerError.contains("connection")) {
            return "Please check network connectivity and try again.";
        }

        // Общий совет по умолчанию
        return "Please try again later or contact the development team if the issue persists.";
    }

    /**
     * Проверяет, нужно ли публиковать комментарий с результатами анализа.
     * Комментарий публикуется только если найдены проблемы.
     *
     * @param analysisResult результаты анализа
     * @return true если нужно опубликовать комментарий, false если результат пустой
     */
    public boolean shouldPostComment(AnalysisResult analysisResult) {
        return analysisResult != null && !analysisResult.isEmpty();
    }
}
