package com.mr.checker.model.gitlab;

import lombok.Data;

import java.util.List;

/**
 * Represents the complete changes information from GitLab API response.
 * Contains MR metadata and list of file changes (diffs).
 */
@Data
public class MRChanges {

    /**
     * Merge request ID.
     */
    private Integer id;

    /**
     * Merge request IID (internal ID within the project).
     */
    private Integer iid;

    /**
     * Merge request title.
     */
    private String title;

    /**
     * Merge request description.
     */
    private String description;

    /**
     * List of file changes in this merge request.
     */
    private List<DiffFile> changes;

    /**
     * Returns the total number of files changed in this merge request.
     *
     * @return number of changed files, 0 if changes list is null
     */
    public Integer getFileCount() {
        return changes != null ? changes.size() : 0;
    }

    /**
     * Returns the total number of changed lines across all files in this merge request.
     * Counts both additions (+) and deletions (-) in diff content.
     *
     * @return total number of changed lines, 0 if no changes or changes list is empty
     */
    public Integer getTotalChangedLines() {
        if (changes == null || changes.isEmpty()) {
            return 0;
        }

        return changes.stream()
                .mapToInt(this::countLinesInDiff)
                .sum();
    }

    private Integer countLinesInDiff(DiffFile diffFile) {
        if (diffFile.getDiff() == null) {
            return 0;
        }

        String[] lines = diffFile.getDiff().split("\\n");
        int changedLines = 0;

        for (String line : lines) {
            // Считаем только строки, начинающиеся с + или -
            if (line.startsWith("+") || line.startsWith("-")) {
                // Не считаем заголовки @@ как изменения
                if (!line.startsWith("@@")) {
                    changedLines++;
                }
            }
        }

        return changedLines;
    }
}
