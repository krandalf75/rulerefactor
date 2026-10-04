package io.github.krandalf75.rulerefactor.core.service;

import io.github.krandalf75.rulerefactor.core.model.CodeIssue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IssueSelector {
    private IssueSelector() {
    }

    public static List<CodeIssue> filterByRules(List<CodeIssue> issues, String[] rules) {
        if (rules == null || rules.length == 0) {
            return issues;
        }

        Set<String> allowed = new HashSet<>();
        for (String rule : rules) {
            if (rule != null && !rule.isBlank()) {
                allowed.add(rule.trim());
            }
        }

        return issues.stream()
                .filter(issue -> allowed.contains(issue.ruleKey().value()))
                .toList();
    }

    public static List<CodeIssue> filterByIssueIds(List<CodeIssue> issues, String[] issueIds) {
        if (issueIds == null || issueIds.length == 0) {
            return issues;
        }

        Set<String> allowed = new HashSet<>();
        for (String issueId : issueIds) {
            if (issueId != null && !issueId.isBlank()) {
                allowed.add(issueId.trim());
            }
        }

        return issues.stream()
                .filter(issue -> allowed.contains(issue.issueId()))
                .toList();
    }

    public static List<CodeIssue> filterBySelection(List<CodeIssue> issues, String[] rules, String[] issueIds) {
        return filterByIssueIds(filterByRules(issues, rules), issueIds);
    }
}
