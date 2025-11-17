package com.mr.checker.util;

import com.mr.checker.model.AnalysisResult;
import com.mr.checker.model.response.CategoryResult;
import com.mr.checker.model.response.Issue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Утилита для форматирования результатов анализа в Markdown формат для комментариев GitLab.
 */
public class MarkdownFormatter {

    /**
     * Форматирует одну категорию результатов в Markdown.
     *
     * @param category категория с результатами анализа
     * @return отформатированная строка Markdown
     */
    public String formatCategory(CategoryResult category) {
        StringBuilder sb = new StringBuilder();

        // Заголовок категории с иконкой и количеством issues
        sb.append("## ").append(getCategoryIcon(category.getCategory()))
          .append(" ").append(formatCategoryName(category.getCategory()))
          .append("\n");
        sb.append("(").append(category.getIssuesCount()).append(" issues)\n\n");

        // Форматирование каждого issue
        for (var issue : category.getIssues()) {
            sb.append("### ").append(getSeverityIcon(issue.getSeverity()))
              .append(" ").append(issue.getSeverity().name().toUpperCase())
              .append(": ").append(issue.getDescription()).append("\n");
            sb.append("**Recommendation:** ").append(issue.getRecommendation()).append("\n\n");
        }

        return sb.toString().trim();
    }

    /**
     * Форматирует отдельное issue в Markdown.
     *
     * @param issue issue для форматирования
     * @return отформатированная строка Markdown
     */
    public String formatIssue(Issue issue) {
        return "### " + getSeverityIcon(issue.getSeverity()) + " " +
               issue.getSeverity().name().toUpperCase() + ": " +
               issue.getDescription() + "\n" +
               "**Recommendation:** " + issue.getRecommendation();
    }

    /**
     * Форматирует полный результат анализа в Markdown для комментария GitLab.
     *
     * @param analysisResult результат анализа кода
     * @return полный отформатированный комментарий в Markdown
     */
    public String formatAnalysisResults(AnalysisResult analysisResult) {
        StringBuilder sb = new StringBuilder();

        // Заголовок
        sb.append("# 🔍 Code Analysis Results\n\n");

        // Общее количество проблем
        int totalIssues = analysisResult.getTotalIssuesCount();
        sb.append("**Total issues found:** ").append(totalIssues).append("\n\n");

        // Форматирование каждой категории в определенном порядке
        // Security first (most important), then Performance, Logical, Best Practices
        List<CategoryResult> categories = analysisResult.getCategoryResults();

        // Создаем категории в нужном порядке
        CategoryResult securityCategory = categories.stream()
            .filter(c -> "security".equals(c.getCategory()) || "security-vulnerabilities".equals(c.getCategory()))
            .findFirst().orElse(null);

        CategoryResult performanceCategory = categories.stream()
            .filter(c -> "performance".equals(c.getCategory()) || "performance-issues".equals(c.getCategory()))
            .findFirst().orElse(null);

        CategoryResult logicalCategory = categories.stream()
            .filter(c -> "logical".equals(c.getCategory()) || "logical-errors".equals(c.getCategory()))
            .findFirst().orElse(null);

        CategoryResult bestPracticesCategory = categories.stream()
            .filter(c -> "best-practices".equals(c.getCategory()) || "best-practices-violations".equals(c.getCategory()))
            .findFirst().orElse(null);

        // Форматируем в нужном порядке
        if (securityCategory != null) {
            sb.append(formatCategory(securityCategory)).append("\n\n");
        }
        if (performanceCategory != null) {
            sb.append(formatCategory(performanceCategory)).append("\n\n");
        }
        if (logicalCategory != null) {
            sb.append(formatCategory(logicalCategory)).append("\n\n");
        }
        if (bestPracticesCategory != null) {
            sb.append(formatCategory(bestPracticesCategory)).append("\n\n");
        }

        // Разделитель и timestamp
        sb.append("---\n");
        sb.append("*Analysis completed at: ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
          .append("*");

        return sb.toString();
    }

    /**
     * Возвращает иконку для категории.
     */
    private String getCategoryIcon(String category) {
        return switch (category) {
            case "security", "security-vulnerabilities" -> "🔴";
            case "logical", "logical-errors" -> "🧠";
            case "performance", "performance-issues" -> "⚡";
            case "best-practices", "best-practices-violations" -> "📚";
            default -> "❓";
        };
    }

    /**
     * Форматирует название категории для заголовка.
     */
    private String formatCategoryName(String category) {
        return switch (category) {
            case "security", "security-vulnerabilities" -> "Security Vulnerabilities";
            case "logical", "logical-errors" -> "Logical Errors";
            case "performance", "performance-issues" -> "Performance Issues";
            case "best-practices", "best-practices-violations" -> "Best Practices Violations";
            default -> category.replace("-", " ").toUpperCase();
        };
    }

    /**
     * Возвращает иконку для уровня severity.
     */
    private String getSeverityIcon(com.mr.checker.model.response.Severity severity) {
        return switch (severity) {
            case HIGH -> "🚨";
            case MEDIUM -> "⚠️";
            case LOW -> "ℹ️";
            default -> "❓";
        };
    }
}
