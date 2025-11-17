package com.mr.checker.service;

import com.mr.checker.client.GitLabApiClient;
import com.mr.checker.model.AnalysisResult;
import com.mr.checker.model.response.Issue;
import com.mr.checker.model.response.Severity;
import com.mr.checker.util.MarkdownFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private GitLabApiClient gitLabApiClient;

    @Mock
    private MarkdownFormatter markdownFormatter;

    @InjectMocks
    private CommentService commentService;

    @Test
    void postAnalysisComment_shouldFormatAndPostCommentWhenIssuesFound() {
        // Given
        Long projectId = 123L;
        Long mrIid = 456L;

        Issue issue = new Issue();
        issue.setSeverity(Severity.HIGH);
        issue.setDescription("SQL injection vulnerability");
        issue.setRecommendation("Use PreparedStatement");

        AnalysisResult analysisResult = new AnalysisResult();
        analysisResult.setSecurityVulnerabilities(List.of(issue));

        String formattedComment = "# 🔍 Code Analysis Results\n\n**Total issues found:** 1\n\n## 🔴 Security Vulnerabilities\n(1 issues)\n\n### 🚨 HIGH: SQL injection vulnerability\n**Recommendation:** Use PreparedStatement";

        when(markdownFormatter.formatAnalysisResults(analysisResult)).thenReturn(formattedComment);

        // When
        commentService.postAnalysisComment(projectId, mrIid, analysisResult);

        // Then
        verify(markdownFormatter).formatAnalysisResults(analysisResult);
        verify(gitLabApiClient).postComment(eq(projectId), eq(mrIid), eq(formattedComment));
    }

    @Test
    void postAnalysisComment_shouldNotPostCommentWhenNoIssues() {
        // Given
        Long projectId = 123L;
        Long mrIid = 456L;

        AnalysisResult analysisResult = new AnalysisResult(); // Empty result

        // When
        commentService.postAnalysisComment(projectId, mrIid, analysisResult);

        // Then
        verify(markdownFormatter, never()).formatAnalysisResults(any());
        verify(gitLabApiClient, never()).postComment(anyLong(), anyLong(), any());
    }

    @Test
    void postAnalysisComment_shouldPostCommentWithMultipleCategories() {
        // Given
        Long projectId = 123L;
        Long mrIid = 456L;

        Issue securityIssue = new Issue();
        securityIssue.setSeverity(Severity.HIGH);
        securityIssue.setDescription("SQL injection");

        Issue logicalIssue = new Issue();
        logicalIssue.setSeverity(Severity.MEDIUM);
        logicalIssue.setDescription("Null pointer exception");

        AnalysisResult analysisResult = new AnalysisResult();
        analysisResult.setSecurityVulnerabilities(List.of(securityIssue));
        analysisResult.setLogicalErrors(List.of(logicalIssue));

        String formattedComment = "# 🔍 Code Analysis Results\n\n**Total issues found:** 2\n\n## 🔴 Security Vulnerabilities\n(1 issues)\n\n### 🚨 HIGH: SQL injection\n\n## 🧠 Logical Errors\n(1 issues)\n\n### ⚠️ MEDIUM: Null pointer exception";

        when(markdownFormatter.formatAnalysisResults(analysisResult)).thenReturn(formattedComment);

        // When
        commentService.postAnalysisComment(projectId, mrIid, analysisResult);

        // Then
        verify(markdownFormatter).formatAnalysisResults(analysisResult);
        verify(gitLabApiClient).postComment(eq(projectId), eq(mrIid), eq(formattedComment));
    }

    @Test
    void shouldPostComment_shouldReturnTrueWhenIssuesExist() {
        // Given
        AnalysisResult analysisResult = new AnalysisResult();
        Issue issue = new Issue();
        analysisResult.setSecurityVulnerabilities(List.of(issue));

        // When
        boolean shouldPost = commentService.shouldPostComment(analysisResult);

        // Then
        assertThat(shouldPost).isTrue();
    }

    @Test
    void shouldPostComment_shouldReturnFalseWhenNoIssues() {
        // Given
        AnalysisResult analysisResult = new AnalysisResult();

        // When
        boolean shouldPost = commentService.shouldPostComment(analysisResult);

        // Then
        assertThat(shouldPost).isFalse();
    }

    @Test
    void postErrorComment_shouldCreateAndPostErrorComment() {
        // Given
        Long projectId = 123L;
        Long mrIid = 456L;
        String errorMessage = "Failed to analyze code due to LLM timeout";

        String expectedComment = "## ❌ Analysis Error\n\n" +
                "Code analysis could not be completed due to the following error:\n\n" +
                "**Error:** Failed to analyze code due to LLM timeout\n\n" +
                "Please try again later. The analysis service may be temporarily overloaded.";

        // When
        commentService.postErrorComment(projectId, mrIid, errorMessage);

        // Then
        verify(gitLabApiClient).postComment(eq(projectId), eq(mrIid), eq(expectedComment));
    }

    @Test
    void postErrorComment_shouldHandleNullErrorMessage() {
        // Given
        Long projectId = 123L;
        Long mrIid = 456L;
        String errorMessage = null;

        String expectedComment = "## ❌ Analysis Error\n\n" +
                "Code analysis could not be completed due to the following error:\n\n" +
                "**Error:** Unknown error occurred\n\n" +
                "Please try again later or contact the development team if the issue persists.";

        // When
        commentService.postErrorComment(projectId, mrIid, errorMessage);

        // Then
        verify(gitLabApiClient).postComment(eq(projectId), eq(mrIid), eq(expectedComment));
    }

    @Test
    void postErrorComment_shouldHandleGitLabError() {
        // Given
        Long projectId = 123L;
        Long mrIid = 456L;
        String errorMessage = "GitLab API authentication failed";

        String expectedComment = "## ❌ Analysis Error\n\n" +
                "Code analysis could not be completed due to the following error:\n\n" +
                "**Error:** GitLab API authentication failed\n\n" +
                "Please check the GitLab token configuration or contact the development team.";

        // When
        commentService.postErrorComment(projectId, mrIid, errorMessage);

        // Then
        verify(gitLabApiClient).postComment(eq(projectId), eq(mrIid), eq(expectedComment));
    }

    @Test
    void postErrorComment_shouldHandleLLMError() {
        // Given
        Long projectId = 123L;
        Long mrIid = 456L;
        String errorMessage = "LLM service unavailable";

        String expectedComment = "## ❌ Analysis Error\n\n" +
                "Code analysis could not be completed due to the following error:\n\n" +
                "**Error:** LLM service unavailable\n\n" +
                "Please try again later. The analysis service may be temporarily overloaded.";

        // When
        commentService.postErrorComment(projectId, mrIid, errorMessage);

        // Then
        verify(gitLabApiClient).postComment(eq(projectId), eq(mrIid), eq(expectedComment));
    }
}
