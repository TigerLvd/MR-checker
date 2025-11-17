package com.mr.checker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiffParserTest {

    private final DiffParser diffParser = new DiffParser();

    @Test
    void parseDeltas_shouldParseSimpleAdditionDiff() {
        // Given
        String diff = """
                diff --git a/src/main/java/Test.java b/src/main/java/Test.java
                index 1234567..abcdef0 100644
                --- a/src/main/java/Test.java
                +++ b/src/main/java/Test.java
                @@ -1,3 +1,4 @@
                 public class Test {
                     private String name;
                 +
                 }
                """;

        // When
        List<String> deltas = diffParser.parseDeltas(diff);

        // Then
        assertThat(deltas).hasSize(1);
        assertThat(deltas.get(0)).contains("@@ -1,3 +1,4 @@");
        assertThat(deltas.get(0)).contains("public class Test {");
        assertThat(deltas.get(0)).contains("private String name;");
        assertThat(deltas.get(0)).contains("+");
        assertThat(deltas.get(0)).contains("}");
    }

    @Test
    void parseDeltas_shouldParseMultipleHunks() {
        // Given
        String diff = """
                diff --git a/src/main/java/Test.java b/src/main/java/Test.java
                index 1234567..abcdef0 100644
                --- a/src/main/java/Test.java
                +++ b/src/main/java/Test.java
                @@ -1,3 +1,4 @@
                 public class Test {
                     private String name;
                 +
                 }
                @@ -10,2 +11,4 @@
                 public void method() {
                 +    // new comment
                 +    System.out.println("test");
                 }
                """;

        // When
        List<String> deltas = diffParser.parseDeltas(diff);

        // Then
        assertThat(deltas).hasSize(2);
        assertThat(deltas.get(0)).contains("@@ -1,3 +1,4 @@");
        assertThat(deltas.get(1)).contains("@@ -10,2 +11,4 @@");
    }

    @Test
    void parseDeltas_shouldReturnEmptyListWhenNoDiffContent() {
        // Given
        String diff = """
                diff --git a/README.md b/README.md
                index 1234567..abcdef0 100644
                --- a/README.md
                +++ b/README.md
                """;

        // When
        List<String> deltas = diffParser.parseDeltas(diff);

        // Then
        assertThat(deltas).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "some random text without diff",
        "diff --git a/file.txt b/file.txt\nindex 123..456\n--- a/file.txt\n+++ b/file.txt"
    })
    void parseDeltas_shouldHandleEdgeCases(String diff) {
        // When
        List<String> deltas = diffParser.parseDeltas(diff);

        // Then
        assertThat(deltas).isEmpty();
    }

    @Test
    void extractChangedLines_shouldExtractAdditionsAndDeletions() {
        // Given
        String hunk = """
                @@ -1,3 +1,4 @@
                 public class Test {
                -    private String name;
                +    private String fullName;
                 }
                +}
                """;

        // When
        List<ChangedLine> changes = diffParser.extractChangedLines(hunk);

        // Then
        assertThat(changes).hasSize(3);
        assertThat(changes.get(0).getType()).isEqualTo(ChangedLine.ChangeType.DELETION);
        assertThat(changes.get(0).getContent()).isEqualTo("    private String name;");
        assertThat(changes.get(1).getType()).isEqualTo(ChangedLine.ChangeType.ADDITION);
        assertThat(changes.get(1).getContent()).isEqualTo("    private String fullName;");
        assertThat(changes.get(2).getType()).isEqualTo(ChangedLine.ChangeType.ADDITION);
        assertThat(changes.get(2).getContent()).isEqualTo("}");
    }

    @Test
    void extractChangedLines_shouldHandleContextLines() {
        // Given
        String hunk = """
@@ -1,5 +1,5 @@
 public class Test {
     private String name;
-    public void oldMethod() {
+    public void newMethod() {
        System.out.println("test");
     }
 }
""";

        // When
        List<ChangedLine> changes = diffParser.extractChangedLines(hunk);

        // Then
        assertThat(changes).hasSize(2);
        // Контекстные строки игнорируются, только изменения
        assertThat(changes.stream().allMatch(ChangedLine::isCodeChange)).isTrue();
    }

    @Test
    void extractChangedLines_shouldReturnEmptyListForNoChanges() {
        // Given
        String hunk = """
                @@ -1,3 +1,3 @@
                 public class Test {
                     private String name;
                 }
                """;

        // When
        List<ChangedLine> changes = diffParser.extractChangedLines(hunk);

        // Then
        assertThat(changes).isEmpty();
    }

    @Test
    void extractChangedLines_shouldHandleEmptyHunk() {
        // Given
        String hunk = "";

        // When
        List<ChangedLine> changes = diffParser.extractChangedLines(hunk);

        // Then
        assertThat(changes).isEmpty();
    }

    @Test
    void filterCodeOnly_shouldFilterOutEmptyLinesAndComments() {
        // Given
        List<ChangedLine> changes = List.of(
            new ChangedLine(1, ChangedLine.ChangeType.ADDITION, "    public void method() {"),
            new ChangedLine(2, ChangedLine.ChangeType.ADDITION, "        // This is a comment"),
            new ChangedLine(3, ChangedLine.ChangeType.ADDITION, ""),
            new ChangedLine(4, ChangedLine.ChangeType.ADDITION, "        System.out.println(\"test\");"),
            new ChangedLine(5, ChangedLine.ChangeType.ADDITION, "    }"),
            new ChangedLine(6, ChangedLine.ChangeType.ADDITION, "        "),
            new ChangedLine(7, ChangedLine.ChangeType.ADDITION, "/* block comment */")
        );

        // When
        List<ChangedLine> filtered = diffParser.filterCodeOnly(changes);

        // Then
        assertThat(filtered).hasSize(3);
        assertThat(filtered.get(0).getContent()).isEqualTo("    public void method() {");
        assertThat(filtered.get(1).getContent()).isEqualTo("        System.out.println(\"test\");");
        assertThat(filtered.get(2).getContent()).isEqualTo("    }");
    }

    @Test
    void filterCodeOnly_shouldHandleWhitespaceOnlyChanges() {
        // Given
        List<ChangedLine> changes = List.of(
            new ChangedLine(1, ChangedLine.ChangeType.ADDITION, "    "),
            new ChangedLine(2, ChangedLine.ChangeType.ADDITION, "\t\t"),
            new ChangedLine(3, ChangedLine.ChangeType.ADDITION, "  \t  "),
            new ChangedLine(4, ChangedLine.ChangeType.ADDITION, "    int x = 1;")
        );

        // When
        List<ChangedLine> filtered = diffParser.filterCodeOnly(changes);

        // Then
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getContent()).isEqualTo("    int x = 1;");
    }

    @Test
    void filterCodeOnly_shouldHandleJavaComments() {
        // Given
        List<ChangedLine> changes = List.of(
            new ChangedLine(1, ChangedLine.ChangeType.ADDITION, "// single line comment"),
            new ChangedLine(2, ChangedLine.ChangeType.ADDITION, "/* block comment start"),
            new ChangedLine(3, ChangedLine.ChangeType.ADDITION, "   still block comment"),
            new ChangedLine(4, ChangedLine.ChangeType.ADDITION, "*/"),
            new ChangedLine(5, ChangedLine.ChangeType.ADDITION, "    /** javadoc comment */"),
            new ChangedLine(6, ChangedLine.ChangeType.ADDITION, "    public void method() {")
        );

        // When
        List<ChangedLine> filtered = diffParser.filterCodeOnly(changes);

        // Then
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getContent()).isEqualTo("    public void method() {");
    }

    @Test
    void filterCodeOnly_shouldReturnEmptyListWhenAllFilteredOut() {
        // Given
        List<ChangedLine> changes = List.of(
            new ChangedLine(1, ChangedLine.ChangeType.ADDITION, "// comment"),
            new ChangedLine(2, ChangedLine.ChangeType.ADDITION, ""),
            new ChangedLine(3, ChangedLine.ChangeType.ADDITION, "    ")
        );

        // When
        List<ChangedLine> filtered = diffParser.filterCodeOnly(changes);

        // Then
        assertThat(filtered).isEmpty();
    }

    @Test
    void filterCodeOnly_shouldHandleEmptyInput() {
        // Given
        List<ChangedLine> changes = List.of();

        // When
        List<ChangedLine> filtered = diffParser.filterCodeOnly(changes);

        // Then
        assertThat(filtered).isEmpty();
    }
}
