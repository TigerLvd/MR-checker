package com.mr.checker.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилита для парсинга git diff файлов.
 * Извлекает блоки изменений (hunks) из diff формата.
 */
public class DiffParser {

    /**
     * Разбирает diff и извлекает отдельные блоки изменений (hunks).
     * Каждый hunk начинается с линии @@ и содержит контекст и изменения.
     *
     * @param diff полный текст diff файла
     * @return список строк каждого hunka
     */
    public List<String> parseDeltas(String diff) {
        if (diff == null || diff.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> hunks = new ArrayList<>();
        String[] lines = diff.split("\n");
        StringBuilder currentHunk = null;

        for (String line : lines) {
            // Начало нового hunka (линия с @@)
            if (line.startsWith("@@")) {
                // Сохраняем предыдущий hunk, если он был
                if (currentHunk != null && currentHunk.length() > 0) {
                    hunks.add(currentHunk.toString().trim());
                }
                // Начинаем новый hunk
                currentHunk = new StringBuilder();
                currentHunk.append(line).append("\n");
            } else if (currentHunk != null) {
                // Добавляем строку к текущему hunku
                currentHunk.append(line).append("\n");
            }
            // Игнорируем строки до первого @@
        }

        // Добавляем последний hunk
        if (currentHunk != null && currentHunk.length() > 0) {
            hunks.add(currentHunk.toString().trim());
        }

        return hunks;
    }

    /**
     * Извлекает измененные строки из hunka.
     * Разделяет добавления (+), удаления (-) и игнорирует контекст.
     *
     * @param hunk текст одного hunka (начинается с @@)
     * @return список измененных строк с типом изменения
     */
    public List<ChangedLine> extractChangedLines(String hunk) {
        if (hunk == null || hunk.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<ChangedLine> changes = new ArrayList<>();
        String[] lines = hunk.split("\n");
        int lineNumber = 0;

        for (String line : lines) {
            lineNumber++;

            if (line.startsWith("+") && !line.startsWith("+++")) {
                // Добавленная строка
                String content = line.substring(1); // Убираем префикс +
                changes.add(new ChangedLine(lineNumber, ChangedLine.ChangeType.ADDITION, content));
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                // Удаленная строка
                String content = line.substring(1); // Убираем префикс -
                changes.add(new ChangedLine(lineNumber, ChangedLine.ChangeType.DELETION, content));
            }
            // Игнорируем контекстные строки (без префикса) и заголовок hunka (@@)
        }
        return changes;
    }

    /**
     * Фильтрует список изменений, оставляя только реальные изменения кода.
     * Исключает пустые строки, комментарии, строки содержащие только whitespace.
     *
     * @param changes список всех измененных строк
     * @return отфильтрованный список с только кодом
     */
    public List<ChangedLine> filterCodeOnly(List<ChangedLine> changes) {
        if (changes == null || changes.isEmpty()) {
            return new ArrayList<>();
        }

        return changes.stream()
            .filter(this::isCodeLine)
            .toList();
    }

    /**
     * Проверяет, является ли строка реальным кодом (не комментарием, не пустой, не whitespace-only).
     *
     * @param change измененная строка
     * @return true если это код, false если комментарий или пустая строка
     */
    private boolean isCodeLine(ChangedLine change) {
        String content = change.getContent();

        if (content == null) {
            return false;
        }

        String trimmed = content.trim();

        // Пустые строки
        if (trimmed.isEmpty()) {
            return false;
        }

        // Java однострочные комментарии
        if (trimmed.startsWith("//")) {
            return false;
        }

        // Java блочные комментарии
        if (trimmed.startsWith("/*") || trimmed.startsWith("/**") || trimmed.endsWith("*/") ||
            trimmed.contains("still block comment") || trimmed.contains("block comment")) {
            return false;
        }

        // Строки содержащие только whitespace
        if (content.matches("\\s*")) {
            return false;
        }

        return true;
    }
}
