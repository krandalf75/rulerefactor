package io.github.krandalf75.rulerefactor.core.service;

import io.github.krandalf75.rulerefactor.core.api.IssueDetector;
import io.github.krandalf75.rulerefactor.core.api.IssueFixer;
import io.github.krandalf75.rulerefactor.core.api.RuleRegistry;
import io.github.krandalf75.rulerefactor.core.model.ApplyEntry;
import io.github.krandalf75.rulerefactor.core.model.ApplyReport;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class RefactorEngine {
    private final RuleRegistry ruleRegistry;

    public RefactorEngine(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    public List<CodeIssue> listIssues(Path projectPath, String[] rules) {
        List<CodeIssue> issues = new ArrayList<>();
        List<IssueDetector> detectors = ruleRegistry.capabilities().stream()
                .filter(capability -> capability.detectable())
                .map(capability -> ruleRegistry.detector(capability.ruleKey()).orElse(null))
                .filter(detector -> detector != null)
                .toList();

        for (IssueDetector detector : detectors) {
            issues.addAll(detector.detect(projectPath));
        }

        List<CodeIssue> filtered = IssueSelector.filterByRules(issues, rules);
        return filtered.stream()
                .sorted(Comparator
                        .comparing(CodeIssue::filePath)
                        .thenComparing(issue -> issue.line() == null ? Integer.MAX_VALUE : issue.line()))
                .toList();
    }

    public ApplyReport applySelected(Path projectPath, String[] rules, String[] issueIds, boolean dryRun) {
        return applySelected(projectPath, rules, issueIds, dryRun, null, null);
    }

    public ApplyReport applySelected(Path projectPath, String[] rules, String[] issueIds, boolean dryRun, Consumer<String> progress) {
        return applySelected(projectPath, rules, issueIds, dryRun, null, progress);
    }

    public ApplyReport applySelected(Path projectPath, String[] rules, String[] issueIds, boolean dryRun, String[] verifyHooks, Consumer<String> progress) {
        List<CodeIssue> initiallyDetected = listIssues(projectPath, null);
        emit(progress, "Detected " + initiallyDetected.size() + " issues.");
        Map<String, CodeIssue> initialById = new HashMap<>();
        for (CodeIssue issue : initiallyDetected) {
            initialById.put(issue.issueId(), issue);
        }

        List<ApplyEntry> entries = new ArrayList<>();
        int applied = 0;
        int skipped = 0;
        int skippedOutdated = 0;
        int failed = 0;

        List<String> requestedIds = new ArrayList<>();
        if (issueIds != null) {
            for (String issueId : issueIds) {
                if (issueId != null && !issueId.isBlank()) {
                    requestedIds.add(issueId.trim());
                }
            }
        }

        List<CodeIssue> currentIssues = listIssues(projectPath, rules);
        emit(progress, "Prepared " + currentIssues.size() + " candidate issues for apply.");
        if (requestedIds.isEmpty()) {
            Set<String> appliedPerFileRule = new HashSet<>();
            int total = currentIssues.size();
            int idx = 0;
            for (CodeIssue current : currentIssues) {
                idx++;
                emit(progress, "Processing " + idx + "/" + total + ": " + current.issueId() + " " + current.ruleKey().value());
                String fileRuleKey = current.filePath() + "|" + current.ruleKey().value();
                if (!appliedPerFileRule.add(fileRuleKey)) {
                    skipped++;
                    entries.add(new ApplyEntry(current.issueId(), current.ruleKey().value(), "skipped_already_covered", "File/rule already processed in this apply pass"));
                    continue;
                }

                IssueFixer fixer = ruleRegistry.fixer(current.ruleKey()).orElse(null);
                if (fixer == null || !fixer.supports(current)) {
                    skipped++;
                    entries.add(new ApplyEntry(current.issueId(), current.ruleKey().value(), "skipped_not_fixable", "No fixer available"));
                    continue;
                }
                String beforeContent = snapshot(projectPath, current.filePath());
                var outcome = fixer.apply(current, projectPath, dryRun);
                if (outcome.applied()) {
                    String verificationError = dryRun ? null : verify(projectPath, verifyHooks);
                    if (verificationError != null) {
                        failed++;
                        restore(projectPath, current.filePath(), beforeContent);
                        emit(progress, "Failed: " + current.issueId() + " (" + current.ruleKey().value() + ") - " + verificationError);
                        entries.add(new ApplyEntry(current.issueId(), current.ruleKey().value(), "failed", "Verification failed and changes were rolled back: " + verificationError));
                    } else {
                        applied++;
                        emit(progress, "Applied: " + current.issueId() + " (" + current.ruleKey().value() + ")");
                        entries.add(new ApplyEntry(current.issueId(), current.ruleKey().value(), "applied", outcome.details()));
                    }
                } else {
                    skipped++;
                    emit(progress, "Skipped: " + current.issueId() + " (" + current.ruleKey().value() + ") - " + outcome.details());
                    entries.add(new ApplyEntry(current.issueId(), current.ruleKey().value(), "skipped_not_fixable", outcome.details()));
                }
            }
            return new ApplyReport(initiallyDetected.size(), initiallyDetected.size(), applied, skipped, skippedOutdated, failed, entries);
        }

        Set<String> appliedPerFileRule = new HashSet<>();
        int total = requestedIds.size();
        int idx = 0;

        for (String selectedId : requestedIds) {
            idx++;
            emit(progress, "Processing selected " + idx + "/" + total + ": " + selectedId);
            currentIssues = listIssues(projectPath, rules);
            Map<String, CodeIssue> currentById = new HashMap<>();
            for (CodeIssue issue : currentIssues) {
                currentById.put(issue.issueId(), issue);
            }

            CodeIssue target = currentById.get(selectedId);
            if (target == null) {
                target = findBySignature(currentIssues, initialById.get(selectedId));
            }

            if (target == null) {
                skippedOutdated++;
                String ruleKey = initialById.containsKey(selectedId) ? initialById.get(selectedId).ruleKey().value() : "unknown";
                entries.add(new ApplyEntry(selectedId, ruleKey, "skipped_outdated", "Selected issue no longer present"));
                continue;
            }

            IssueFixer fixer = ruleRegistry.fixer(target.ruleKey()).orElse(null);
            if (fixer == null || !fixer.supports(target)) {
                skipped++;
                entries.add(new ApplyEntry(selectedId, target.ruleKey().value(), "skipped_not_fixable", "No fixer available"));
                continue;
            }

            String fileRuleKey = target.filePath() + "|" + target.ruleKey().value();
            if (!appliedPerFileRule.add(fileRuleKey)) {
                skipped++;
                entries.add(new ApplyEntry(selectedId, target.ruleKey().value(), "skipped_already_covered", "File/rule already processed in this apply pass"));
                continue;
            }

            String beforeContent = snapshot(projectPath, target.filePath());
            var outcome = fixer.apply(target, projectPath, dryRun);
            if (outcome.applied()) {
                String verificationError = dryRun ? null : verify(projectPath, verifyHooks);
                if (verificationError != null) {
                    failed++;
                    restore(projectPath, target.filePath(), beforeContent);
                    emit(progress, "Failed: " + selectedId + " (" + target.ruleKey().value() + ") - " + verificationError);
                    entries.add(new ApplyEntry(selectedId, target.ruleKey().value(), "failed", "Verification failed and changes were rolled back: " + verificationError));
                } else {
                    applied++;
                    emit(progress, "Applied: " + selectedId + " (" + target.ruleKey().value() + ")");
                    entries.add(new ApplyEntry(selectedId, target.ruleKey().value(), "applied", outcome.details()));
                }
            } else {
                skipped++;
                emit(progress, "Skipped: " + selectedId + " (" + target.ruleKey().value() + ") - " + outcome.details());
                entries.add(new ApplyEntry(selectedId, target.ruleKey().value(), "skipped_not_fixable", outcome.details()));
            }
        }

        return new ApplyReport(initiallyDetected.size(), requestedIds.size(), applied, skipped, skippedOutdated, failed, entries);
    }

    private CodeIssue findBySignature(List<CodeIssue> currentIssues, CodeIssue original) {
        if (original == null) {
            return null;
        }
        return currentIssues.stream()
                .filter(issue -> issue.ruleKey().value().equals(original.ruleKey().value()))
                .filter(issue -> Objects.equals(issue.filePath(), original.filePath()))
                .filter(issue -> Objects.equals(issue.message(), original.message()))
                .findFirst()
                .orElse(null);
    }

    private void emit(Consumer<String> progress, String message) {
        if (progress != null) {
            progress.accept(message);
        }
    }

    private String snapshot(Path projectPath, String relativeFilePath) {
        try {
            return Files.readString(projectPath.resolve(relativeFilePath).normalize(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private void restore(Path projectPath, String relativeFilePath, String content) {
        if (content == null) {
            return;
        }
        try {
            Files.writeString(projectPath.resolve(relativeFilePath).normalize(), content, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }

    private String verify(Path projectPath, String[] verifyHooks) {
        if (verifyHooks == null || verifyHooks.length == 0) {
            return null;
        }
        for (String hook : verifyHooks) {
            if (hook == null || hook.isBlank()) {
                continue;
            }
            String normalized = hook.trim().toLowerCase();
            if (!"compile".equals(normalized) && !"test".equals(normalized)) {
                return "Unknown verification hook: " + hook;
            }
            String command = "compile".equals(normalized)
                    ? "./mvnw -q -DskipTests compile"
                    : "./mvnw -q test";
            try {
                Process process = new ProcessBuilder("zsh", "-lc", command)
                        .directory(projectPath.toFile())
                        .redirectErrorStream(true)
                        .start();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    return normalized + " hook failed with exit code " + exitCode;
                }
            } catch (Exception e) {
                return normalized + " hook failed: " + e.getMessage();
            }
        }
        return null;
    }

}
