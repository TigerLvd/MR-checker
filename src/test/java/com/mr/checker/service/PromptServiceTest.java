package com.mr.checker.service;

import com.mr.checker.model.gitlab.DiffFile;
import com.mr.checker.model.gitlab.MRChanges;
import com.mr.checker.model.llm.Message;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromptServiceTest {

    private final PromptService promptService = new PromptService();

    @Test
    void buildSystemPrompt_shouldReturnInstructionsWithFourCategories() {
        // When
        String systemPrompt = promptService.buildSystemPrompt();

        // Then
        assertThat(systemPrompt).isNotNull();
        assertThat(systemPrompt).isNotEmpty();

        // Проверяем наличие всех четырех категорий анализа
        assertThat(systemPrompt.toLowerCase()).contains("logical errors");
        assertThat(systemPrompt.toLowerCase()).contains("security vulnerabilities");
        assertThat(systemPrompt.toLowerCase()).contains("best practices violations");
        assertThat(systemPrompt.toLowerCase()).contains("performance issues");

        // Проверяем структуру инструкций
        assertThat(systemPrompt).contains("JSON");
        assertThat(systemPrompt).contains("categories");
        assertThat(systemPrompt).contains("severity");
        assertThat(systemPrompt).contains("description");
        assertThat(systemPrompt).contains("recommendation");

        // Проверяем, что это системный промпт для LLM
        assertThat(systemPrompt).contains("code analysis");
        assertThat(systemPrompt).contains("merge request");
        assertThat(systemPrompt).contains("review");
    }

    @Test
    void formatCodeForAnalysis_shouldFormatDiffFileWithFileNamesAndLineNumbers() {
        // Given
        DiffFile diffFile = new DiffFile();
        diffFile.setOldPath("src/main/java/OldClass.java");
        diffFile.setNewPath("src/main/java/NewClass.java");
        diffFile.setDiff("""
                @@ -1,5 +1,6 @@
                 public class OldClass {
                     private String name;
                 +
                 }
                @@ -10,3 +11,5 @@
                 public void method() {
                 +    System.out.println("added");
                 +    return true;
                 }
                """);

        List<DiffFile> diffFiles = List.of(diffFile);

        // When
        String formattedCode = promptService.formatCodeForAnalysis(diffFiles);

        // Then
        assertThat(formattedCode).isNotNull();
        assertThat(formattedCode).contains("File: src/main/java/NewClass.java");

        // Проверяем форматирование изменений
        assertThat(formattedCode).contains("public class OldClass");
        assertThat(formattedCode).contains("private String name;");
        assertThat(formattedCode).contains("+");
        assertThat(formattedCode).contains("+    System.out.println(\"added\")");
        assertThat(formattedCode).contains("+    return true;");
    }

    @Test
    void formatCodeForAnalysis_shouldHandleEmptyDiff() {
        // Given
        DiffFile diffFile = new DiffFile();
        diffFile.setNewPath("src/main/java/Test.java");
        diffFile.setDiff("");

        List<DiffFile> diffFiles = List.of(diffFile);

        // When
        String formattedCode = promptService.formatCodeForAnalysis(diffFiles);

        // Then
        assertThat(formattedCode).isNotNull();
        assertThat(formattedCode).contains("File: src/main/java/Test.java");
        assertThat(formattedCode).doesNotContain("Line");
    }

    @Test
    void formatCodeForAnalysis_shouldHandleMultipleFiles() {
        // Given
        DiffFile file1 = new DiffFile();
        file1.setNewPath("src/main/java/File1.java");
        file1.setDiff("@@ -1,1 +1,1 @@\n-old line\n+new line");

        DiffFile file2 = new DiffFile();
        file2.setNewPath("src/test/java/File2.java");
        file2.setDiff("@@ -1,1 +1,1 @@\n-test line\n+updated line");

        List<DiffFile> diffFiles = List.of(file1, file2);

        // When
        String formattedCode = promptService.formatCodeForAnalysis(diffFiles);

        // Then
        assertThat(formattedCode).contains("File: src/main/java/File1.java");
        assertThat(formattedCode).contains("File: src/test/java/File2.java");
        assertThat(formattedCode).contains("new line");
        assertThat(formattedCode).contains("updated line");
    }

    @Test
    void buildAnalysisPrompt_shouldCombineSystemPromptAndFormattedCode() {
        // Given
        MRChanges mrChanges = new MRChanges();
        mrChanges.setTitle("Fix SQL injection vulnerability");
        mrChanges.setDescription("Security fix for user authentication");

        DiffFile diffFile = new DiffFile();
        diffFile.setNewPath("src/main/java/UserService.java");
        diffFile.setDiff("@@ -1,3 +1,3 @@\n-    String query = \"SELECT * FROM users WHERE id = \" + userId;\n+    String query = \"SELECT * FROM users WHERE id = ?\";");

        mrChanges.setChanges(List.of(diffFile));

        // When
        List<Message> messages = promptService.buildAnalysisPrompt(mrChanges);

        // Then
        assertThat(messages).hasSize(2);

        // Первый message - системный промпт
        Message systemMessage = messages.get(0);
        assertThat(systemMessage.getRole()).isEqualTo("system");
        assertThat(systemMessage.getContent()).contains("code reviewer");
        assertThat(systemMessage.getContent()).contains("four categories");

        // Второй message - код для анализа
        Message userMessage = messages.get(1);
        assertThat(userMessage.getRole()).isEqualTo("user");
        assertThat(userMessage.getContent()).contains("Fix SQL injection vulnerability");
        assertThat(userMessage.getContent()).contains("Security fix for user authentication");
        assertThat(userMessage.getContent()).contains("File: src/main/java/UserService.java");
        assertThat(userMessage.getContent()).contains("String query = \"SELECT * FROM users WHERE id = ?\"");
    }

    @Test
    void buildAnalysisPrompt_shouldHandleMRChangesWithNoChanges() {
        // Given
        MRChanges mrChanges = new MRChanges();
        mrChanges.setTitle("Empty MR");
        mrChanges.setChanges(List.of());

        // When
        List<Message> messages = promptService.buildAnalysisPrompt(mrChanges);

        // Then
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getRole()).isEqualTo("system");
        assertThat(messages.get(1).getRole()).isEqualTo("user");
        assertThat(messages.get(1).getContent()).contains("Empty MR");
        assertThat(messages.get(1).getContent()).doesNotContain("File:");
    }
}
