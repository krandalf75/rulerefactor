package io.github.krandalf75.rulerefactor.core.model;

public record CodeIssue(String issueId, RuleKey ruleKey, String filePath, Integer line, String message) {
}
