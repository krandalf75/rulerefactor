package io.github.krandalf75.rulerefactor.adapters.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class IssueOutputFormatter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private IssueOutputFormatter() {
    }

    public static String format(List<CodeIssue> issues, String format) {
        if ("json".equalsIgnoreCase(format)) {
            return toJson(issues);
        }
        return toTable(issues);
    }

    private static String toJson(List<CodeIssue> issues) {
        List<Map<String, Object>> payload = issues.stream()
                .map(issue -> {
                    Map<String, Object> row = new LinkedHashMap();
                    row.put("issueId", issue.issueId());
                    row.put("ruleKey", issue.ruleKey().value());
                    row.put("filePath", issue.filePath());
                    row.put("line", issue.line());
                    row.put("message", issue.message());
                    return row;
                })
                .toList();
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize issues as JSON", e);
        }
    }

    private static String toTable(List<CodeIssue> issues) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-12s %-6s %-40s %s%n", "IssueID", "Rule", "Line", "File", "Message"));
        sb.append("-".repeat(110)).append(System.lineSeparator());
        for (CodeIssue issue : issues) {
            String line = issue.line() == null || issue.line() < 0 ? "-" : String.valueOf(issue.line());
            sb.append(String.format(
                    "%-10s %-12s %-6s %-40s %s%n",
                    issue.issueId(),
                    issue.ruleKey().value(),
                    line,
                    shorten(issue.filePath(), 40),
                    issue.message()
            ));
        }
        if (issues.isEmpty()) {
            sb.append("No issues found.").append(System.lineSeparator());
        }
        return sb.toString();
    }

    private static String shorten(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return "..." + value.substring(value.length() - (max - 3));
    }
}
