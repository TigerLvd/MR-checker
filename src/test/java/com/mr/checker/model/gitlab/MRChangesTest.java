package com.mr.checker.model.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class MRChangesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeFromGitLabJson() throws Exception {
        // Given - пример полного ответа GitLab API /projects/{id}/merge_requests/{iid}/changes
        String gitLabJson = """
                {
                  "id": 123,
                  "iid": 456,
                  "title": "Fix critical security vulnerability",
                  "description": "This MR fixes a critical security issue in authentication",
                  "changes": [
                    {
                      "old_path": "src/main/java/AuthService.java",
                      "new_path": "src/main/java/AuthService.java",
                      "diff": "@@ -10,3 +10,4 @@\\n-    public boolean authenticate(String user, String pass) {\\n+    public boolean authenticate(String user, String pass) {\\n+        validateInput(user, pass);\\n",
                      "new_file": false,
                      "deleted_file": false
                    },
                    {
                      "old_path": "src/test/java/AuthServiceTest.java",
                      "new_path": "src/test/java/AuthServiceTest.java",
                      "diff": "+    @Test\\n+    void shouldValidateInput() {\\n+        // test code\\n+    }\\n",
                      "new_file": false,
                      "deleted_file": false
                    }
                  ]
                }
                """;

        // When
        MRChanges mrChanges = objectMapper.readValue(gitLabJson, MRChanges.class);

        // Then
        assertThat(mrChanges.getId()).isEqualTo(123);
        assertThat(mrChanges.getIid()).isEqualTo(456);
        assertThat(mrChanges.getTitle()).isEqualTo("Fix critical security vulnerability");
        assertThat(mrChanges.getDescription()).isEqualTo("This MR fixes a critical security issue in authentication");
        assertThat(mrChanges.getChanges()).hasSize(2);

        DiffFile firstChange = mrChanges.getChanges().get(0);
        assertThat(firstChange.getOldPath()).isEqualTo("src/main/java/AuthService.java");
        assertThat(firstChange.getNewPath()).isEqualTo("src/main/java/AuthService.java");
        assertThat(firstChange.getDiff()).contains("validateInput");
    }

    @Test
    void shouldCalculateTotalChangedLines() throws Exception {
        // Given
        DiffFile change1 = new DiffFile();
        change1.setDiff("@@ -1,3 +1,4 @@\n-old line\n+new line\n+added line\n");

        DiffFile change2 = new DiffFile();
        change2.setDiff("@@ -5,2 +5,3 @@\n-old\n+new\n+new2\n");

        MRChanges mrChanges = new MRChanges();
        mrChanges.setChanges(Arrays.asList(change1, change2));

        // When & Then
        assertThat(mrChanges.getTotalChangedLines()).isEqualTo(6); // 3 changes in each diff (1 deletion + 2 additions)
    }

    @Test
    void shouldReturnFileCount() throws Exception {
        // Given
        MRChanges mrChanges = new MRChanges();
        mrChanges.setChanges(Arrays.asList(new DiffFile(), new DiffFile(), new DiffFile()));

        // When & Then
        assertThat(mrChanges.getFileCount()).isEqualTo(3);
    }

    @Test
    void shouldHandleEmptyChanges() throws Exception {
        // Given
        MRChanges mrChanges = new MRChanges();
        mrChanges.setChanges(Arrays.asList());

        // When & Then
        assertThat(mrChanges.getTotalChangedLines()).isEqualTo(0);
        assertThat(mrChanges.getFileCount()).isEqualTo(0);
    }
}
