package com.mr.checker.util;

/**
 * Представляет одну измененную строку в diff.
 */
public class ChangedLine {

    public enum ChangeType {
        ADDITION,    // +
        DELETION,    // -
        CONTEXT      // без префикса (контекст)
    }

    private final int lineNumber;
    private final ChangeType type;
    private final String content;

    public ChangedLine(int lineNumber, ChangeType type, String content) {
        this.lineNumber = lineNumber;
        this.type = type;
        this.content = content;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public ChangeType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public boolean isCodeChange() {
        return type == ChangeType.ADDITION || type == ChangeType.DELETION;
    }

    @Override
    public String toString() {
        return String.format("ChangedLine{line=%d, type=%s, content='%s'}",
                           lineNumber, type, content);
    }
}

