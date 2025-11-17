package com.mr.checker.model.gitlab;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a single file diff from GitLab API response.
 * Contains information about file changes in a merge request.
 */
@Data
public class DiffFile {

    /**
     * Original path of the file before changes.
     * Null for new files.
     */
    @JsonProperty("old_path")
    private String oldPath;

    /**
     * New path of the file after changes.
     * Null for deleted files.
     */
    @JsonProperty("new_path")
    private String newPath;

    /**
     * The actual diff content showing line-by-line changes.
     */
    private String diff;

    /**
     * Indicates if this is a newly added file.
     */
    @JsonProperty("new_file")
    private Boolean newFile;

    /**
     * Indicates if this file was deleted.
     */
    @JsonProperty("deleted_file")
    private Boolean deletedFile;
}
