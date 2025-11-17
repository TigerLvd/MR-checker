package com.mr.checker.service;

import com.mr.checker.model.gitlab.DiffFile;
import com.mr.checker.model.gitlab.MRChanges;
import com.mr.checker.model.llm.Message;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для формирования промптов для LLM анализа кода.
 * Создает системные инструкции и форматирует код для анализа.
 */
@Service
public class PromptService {

    /**
     * Создает системный промпт с инструкциями для LLM анализа кода.
     * Промпт содержит инструкции по анализу четырех категорий проблем:
     * логические ошибки, уязвимости безопасности, нарушения лучших практик, проблемы производительности.
     *
     * @return системный промпт для LLM
     */
    public String buildSystemPrompt() {
        return """
                You are an expert code reviewer performing automated code analysis on merge request changes.
                Your task is to identify issues in the provided code and categorize them into four categories:

                1. LOGICAL ERRORS - Incorrect logic, bugs, potential runtime errors
                2. SECURITY VULNERABILITIES - SQL injection, XSS, authentication issues, etc.
                3. BEST PRACTICES VIOLATIONS - Code style, naming conventions, design patterns
                4. PERFORMANCE ISSUES - Inefficient algorithms, memory leaks, slow operations

                For each issue found, provide:
                - severity: HIGH, MEDIUM, or LOW
                - description: clear explanation of the problem
                - recommendation: specific advice on how to fix it

                Respond with a valid JSON structure containing all categories, even if empty:

                {
                  "logical-errors": [
                    {
                      "severity": "HIGH|MEDIUM|LOW",
                      "description": "Description of the logical error",
                      "recommendation": "How to fix it"
                    }
                  ],
                  "security-vulnerabilities": [],
                  "best-practices-violations": [],
                  "performance-issues": []
                }

                Only report real issues. Do not make assumptions or add issues that aren't clearly present in the code.
                Be specific about line numbers and provide actionable recommendations.
                """;
    }

    /**
     * Форматирует список файлов с изменениями для анализа LLM.
     * Добавляет имена файлов и номера строк для лучшей читаемости.
     *
     * @param diffFiles список файлов с изменениями
     * @return отформатированный код для анализа
     */
    public String formatCodeForAnalysis(List<DiffFile> diffFiles) {
        if (diffFiles == null || diffFiles.isEmpty()) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();

        for (DiffFile diffFile : diffFiles) {
            // Добавляем имя файла
            String fileName = diffFile.getNewPath() != null ? diffFile.getNewPath() :
                            (diffFile.getOldPath() != null ? diffFile.getOldPath() : "unknown");
            formatted.append("File: ").append(fileName).append("\n");

            // Разбираем diff и добавляем номера строк
            String diff = diffFile.getDiff();
            if (diff != null && !diff.trim().isEmpty()) {
                formatted.append(formatDiffWithLineNumbers(diff));
            }

            formatted.append("\n");
        }

        return formatted.toString().trim();
    }

    /**
     * Форматирует diff с номерами строк для каждого hunka.
     *
     * @param diff полный diff текст
     * @return отформатированный diff с номерами строк
     */
    private String formatDiffWithLineNumbers(String diff) {
        StringBuilder result = new StringBuilder();
        String[] lines = diff.split("\n");
        int hunkStartLine = 0;

        for (String line : lines) {
            if (line.startsWith("@@")) {
                // Новый hunk - извлекаем номера строк
                int plusIndex = line.indexOf("@@", line.indexOf("@@") + 1);
                if (plusIndex != -1) {
                    String lineInfo = line.substring(line.indexOf("+"), plusIndex);
                    String[] parts = lineInfo.split(",");
                    if (parts.length >= 1) {
                        try {
                            hunkStartLine = Integer.parseInt(parts[0].substring(1));
                        } catch (NumberFormatException e) {
                            hunkStartLine = 1;
                        }
                    }
                }

                result.append(line).append("\n");
            } else if (!line.trim().isEmpty()) {
                // Обычная строка кода
                result.append("Line ").append(hunkStartLine).append(": ").append(line).append("\n");
                hunkStartLine++;
            }
        }

        return result.toString();
    }

    /**
     * Создает полный промпт для анализа MR, объединяя системные инструкции и код изменений.
     *
     * @param mrChanges изменения в merge request
     * @return список сообщений для отправки в LLM
     */
    public List<Message> buildAnalysisPrompt(MRChanges mrChanges) {
        // Системное сообщение с инструкциями
        Message systemMessage = new Message();
        systemMessage.setRole("system");
        systemMessage.setContent(buildSystemPrompt());

        // Пользовательское сообщение с кодом для анализа
        Message userMessage = new Message();
        userMessage.setRole("user");

        StringBuilder userContent = new StringBuilder();
        userContent.append("Please analyze the following merge request changes:\n\n");

        if (mrChanges.getTitle() != null) {
            userContent.append("Title: ").append(mrChanges.getTitle()).append("\n");
        }

        if (mrChanges.getDescription() != null && !mrChanges.getDescription().trim().isEmpty()) {
            userContent.append("Description: ").append(mrChanges.getDescription()).append("\n\n");
        }

        userContent.append("Code changes:\n");
        String formattedCode = formatCodeForAnalysis(mrChanges.getChanges());
        userContent.append(formattedCode);

        userMessage.setContent(userContent.toString());

        return List.of(systemMessage, userMessage);
    }
}
