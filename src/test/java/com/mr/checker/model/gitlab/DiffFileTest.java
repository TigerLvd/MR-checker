package com.mr.checker.model.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiffFileTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeFromGitLabJson() throws Exception {
        // Given - пример JSON ответа от GitLab API /projects/{id}/merge_requests/{iid}/changes
        String gitLabJson = """
                {
                  "old_path": "src/main/java/com/example/OldClass.java",
                  "new_path": "src/main/java/com/example/NewClass.java",
                  "diff": "@@ -1,3 +1,4 @@\\n-old line\\n+new line\\n+added line\\n",
                  "new_file": false,
                  "deleted_file": false
                }
                """;

        // When
        DiffFile diffFile = objectMapper.readValue(gitLabJson, DiffFile.class);

        // Then
        assertThat(diffFile.getOldPath()).isEqualTo("src/main/java/com/example/OldClass.java");
        assertThat(diffFile.getNewPath()).isEqualTo("src/main/java/com/example/NewClass.java");
        assertThat(diffFile.getDiff()).contains("@@ -1,3 +1,4 @@");
        assertThat(diffFile.getNewFile()).isFalse();
        assertThat(diffFile.getDeletedFile()).isFalse();
    }

    @Test
    void shouldHandleNewFile() throws Exception {
        // Given
        String newFileJson = """
                {
                  "old_path": null,
                  "new_path": "src/main/java/com/example/NewFile.java",
                  "diff": "+new file content\\n",
                  "new_file": true,
                  "deleted_file": false
                }
                """;

        // When
        DiffFile diffFile = objectMapper.readValue(newFileJson, DiffFile.class);

        // Then
        assertThat(diffFile.getOldPath()).isNull();
        assertThat(diffFile.getNewPath()).isEqualTo("src/main/java/com/example/NewFile.java");
        assertThat(diffFile.getNewFile()).isTrue();
        assertThat(diffFile.getDeletedFile()).isFalse();
    }

    @Test
    void shouldHandleDeletedFile() throws Exception {
        // Given
        String deletedFileJson = """
                {
                  "old_path": "src/main/java/com/example/OldFile.java",
                  "new_path": null,
                  "diff": "-deleted file content\\n",
                  "new_file": false,
                  "deleted_file": true
                }
                """;

        // When
        DiffFile diffFile = objectMapper.readValue(deletedFileJson, DiffFile.class);

        // Then
        assertThat(diffFile.getOldPath()).isEqualTo("src/main/java/com/example/OldFile.java");
        assertThat(diffFile.getNewPath()).isNull();
        assertThat(diffFile.getNewFile()).isFalse();
        assertThat(diffFile.getDeletedFile()).isTrue();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        DiffFile diffFile = new DiffFile();
        diffFile.setOldPath("old/path.java");
        diffFile.setNewPath("new/path.java");
        diffFile.setDiff("@@ diff content @@");
        diffFile.setNewFile(false);
        diffFile.setDeletedFile(false);

        // When
        String json = objectMapper.writeValueAsString(diffFile);

        // Then
        assertThat(json).contains("\"old_path\":\"old/path.java\"");
        assertThat(json).contains("\"new_path\":\"new/path.java\"");
        assertThat(json).contains("\"diff\":\"@@ diff content @@\"");
        assertThat(json).contains("\"new_file\":false");
        assertThat(json).contains("\"deleted_file\":false");
    }
}
