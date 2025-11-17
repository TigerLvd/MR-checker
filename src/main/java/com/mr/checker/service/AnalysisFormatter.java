package com.mr.checker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr.checker.model.AnalysisResult;
import com.mr.checker.model.response.Issue;
import com.mr.checker.model.response.Severity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для форматирования результатов анализа от LLM.
 * Парсит JSON ответы и преобразует их в структурированные объекты.
 */
@Slf4j
@Service
public class AnalysisFormatter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Парсит JSON ответ от LLM и преобразует в AnalysisResult.
     * Обрабатывает невалидный JSON с fallback на пустой результат.
     *
     * @param llmResponse JSON строка ответа от LLM
     * @return структурированный результат анализа
     */
    public AnalysisResult parseAnalysisResponse(String llmResponse) {
        AnalysisResult result = new AnalysisResult();

        if (llmResponse == null || llmResponse.trim().isEmpty()) {
            log.warn("LLM response is null or empty");
            return result;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(llmResponse);

            // Парсим каждую категорию
            result.setLogicalErrors(parseIssueList(rootNode, "logical-errors"));
            result.setSecurityVulnerabilities(parseIssueList(rootNode, "security-vulnerabilities"));
            result.setBestPracticesViolations(parseIssueList(rootNode, "best-practices-violations"));
            result.setPerformanceIssues(parseIssueList(rootNode, "performance-issues"));

        } catch (JsonProcessingException e) {
            log.error("Failed to parse LLM response as JSON: {}", e.getMessage());
            // Возвращаем пустой результат при ошибке парсинга
        }

        return result;
    }

    /**
     * Парсит список issues из JSON node.
     *
     * @param rootNode корневой JSON node
     * @param categoryName имя категории
     * @return список issues
     */
    private List<Issue> parseIssueList(JsonNode rootNode, String categoryName) {
        JsonNode categoryNode = rootNode.get(categoryName);
        if (categoryNode == null || !categoryNode.isArray()) {
            return new ArrayList<>();
        }

        List<Issue> issues = new ArrayList<>();
        for (JsonNode issueNode : categoryNode) {
            Issue issue = parseIssue(issueNode);
            if (issue != null) {
                issues.add(issue);
            }
        }

        return issues;
    }

    /**
     * Парсит отдельный issue из JSON node.
     *
     * @param issueNode JSON node с данными issue
     * @return объект Issue или null при ошибке
     */
    private Issue parseIssue(JsonNode issueNode) {
        try {
            String severityStr = issueNode.get("severity").asText();
            String description = issueNode.get("description").asText();
            String recommendation = issueNode.get("recommendation").asText();

            Severity severity = parseSeverity(severityStr);

            Issue issue = new Issue();
            issue.setSeverity(severity);
            issue.setDescription(description);
            issue.setRecommendation(recommendation);

            return issue;

        } catch (Exception e) {
            log.warn("Failed to parse issue from JSON node: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Парсит строковое представление severity в enum.
     *
     * @param severityStr строковое значение severity
     * @return enum значение или LOW по умолчанию
     */
    private Severity parseSeverity(String severityStr) {
        if (severityStr == null) {
            return Severity.LOW;
        }

        try {
            return Severity.valueOf(severityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown severity value: {}, defaulting to LOW", severityStr);
            return Severity.LOW;
        }
    }

    /**
     * Извлекает issues из неструктурированного текста.
     * Использует regex для поиска паттернов severity + описание + рекомендация.
     *
     * @param text неструктурированный текст с описанием проблем
     * @return список найденных issues
     */
    public List<Issue> extractIssues(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Issue> issues = new ArrayList<>();
        String[] lines = text.split("\\n");

        Issue currentIssue = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            // Проверяем, начинается ли строка с severity
            if (line.matches("(?i)(HIGH|MEDIUM|LOW):.*")) {
                // Сохраняем предыдущий issue если он есть
                if (currentIssue != null) {
                    issues.add(currentIssue);
                }

                // Создаем новый issue
                String severityStr = line.substring(0, line.indexOf(":")).toUpperCase();
                String description = line.substring(line.indexOf(":") + 1).trim();

                currentIssue = new Issue();
                currentIssue.setSeverity(parseSeverity(severityStr));
                currentIssue.setDescription(description);
                currentIssue.setRecommendation("Review and fix the issue");
            } else if (currentIssue != null) {
                // Продолжаем описание или ищем рекомендацию
                if (line.toLowerCase().startsWith("fix:") || line.toLowerCase().startsWith("recommendation:")) {
                    String recommendation = line.substring(line.indexOf(":") + 1).trim();
                    currentIssue.setRecommendation(recommendation);
                } else if (!line.startsWith("-") && !line.startsWith("*")) {
                    // Добавляем к описанию, если это не список
                    currentIssue.setDescription(currentIssue.getDescription() + " " + line);
                }
            }
        }

        // Добавляем последний issue
        if (currentIssue != null) {
            issues.add(currentIssue);
        }

        return issues;
    }


    /**
     * Распределяет issues по категориям на основе ключевых слов в описании.
     * Создает AnalysisResult с категоризированными issues.
     *
     * @param issues список issues для категоризации
     * @return AnalysisResult с категоризированными issues
     */
    public AnalysisResult categorizeIssues(List<Issue> issues) {
        AnalysisResult result = new AnalysisResult();

        if (issues == null || issues.isEmpty()) {
            return result;
        }

        List<Issue> logicalErrors = new ArrayList<>();
        List<Issue> securityVulnerabilities = new ArrayList<>();
        List<Issue> bestPracticesViolations = new ArrayList<>();
        List<Issue> performanceIssues = new ArrayList<>();

        for (Issue issue : issues) {
            // Определяем категорию на основе ключевых слов
            String description = issue.getDescription().toLowerCase();

            if (containsSecurityKeywords(description)) {
                securityVulnerabilities.add(issue);
            } else if (containsLogicalKeywords(description)) {
                logicalErrors.add(issue);
            } else if (containsPerformanceKeywords(description)) {
                performanceIssues.add(issue);
            } else if (containsBestPracticesKeywords(description)) {
                bestPracticesViolations.add(issue);
            } else {
                // По умолчанию относим к best practices
                bestPracticesViolations.add(issue);
            }

            // Определяем severity если не установлено
            if (issue.getSeverity() == null) {
                issue.setSeverity(determineSeverity(issue));
            }
        }

        result.setLogicalErrors(logicalErrors);
        result.setSecurityVulnerabilities(securityVulnerabilities);
        result.setBestPracticesViolations(bestPracticesViolations);
        result.setPerformanceIssues(performanceIssues);

        return result;
    }

    /**
     * Определяет severity issue на основе содержания описания.
     * Анализирует ключевые слова для определения уровня критичности.
     *
     * @param issue issue для определения severity
     * @return определенный уровень severity
     */
    public Severity determineSeverity(Issue issue) {
        // Если severity уже установлен, возвращаем его
        if (issue.getSeverity() != null) {
            return issue.getSeverity();
        }

        String description = issue.getDescription().toLowerCase();

        // High severity keywords
        if (description.contains("security") || description.contains("vulnerability") ||
            description.contains("breach") || description.contains("exploit") ||
            description.contains("injection") || description.contains("xss") ||
            description.contains("authentication") || description.contains("authorization")) {
            return Severity.HIGH;
        }

        // Medium severity keywords
        if (description.contains("null pointer") || description.contains("exception") ||
            description.contains("error") || description.contains("crash") ||
            description.contains("memory leak") || description.contains("performance") ||
            description.contains("slow") || description.contains("inefficient")) {
            return Severity.MEDIUM;
        }

        // Low severity по умолчанию
        return Severity.LOW;
    }

    /**
     * Проверяет наличие ключевых слов безопасности.
     */
    private boolean containsSecurityKeywords(String text) {
        String[] keywords = {"security", "vulnerability", "breach", "exploit", "injection",
                           "xss", "csrf", "authentication", "authorization", "password",
                           "encryption", "sql injection", "script injection"};
        return containsAnyKeyword(text, keywords);
    }

    /**
     * Проверяет наличие ключевых слов логических ошибок.
     */
    private boolean containsLogicalKeywords(String text) {
        String[] keywords = {"null pointer", "exception", "error", "crash", "logic",
                           "bug", "incorrect", "wrong", "invalid", "missing"};
        return containsAnyKeyword(text, keywords);
    }

    /**
     * Проверяет наличие ключевых слов производительности.
     */
    private boolean containsPerformanceKeywords(String text) {
        String[] keywords = {"performance", "slow", "inefficient", "memory leak",
                           "complexity", "o(n^2)", "optimization", "bottleneck",
                           "cpu", "memory", "speed"};
        return containsAnyKeyword(text, keywords);
    }

    /**
     * Проверяет наличие ключевых слов лучших практик.
     */
    private boolean containsBestPracticesKeywords(String text) {
        String[] keywords = {"naming", "convention", "style", "comment", "documentation",
                           "pattern", "design", "architecture", "maintainability",
                           "readability", "consistency"};
        return containsAnyKeyword(text, keywords);
    }

    /**
     * Проверяет наличие любого из ключевых слов в тексте.
     */
    private boolean containsAnyKeyword(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
