package io.github.krandalf75.rulerefactor.adapters.agent;

import io.github.krandalf75.rulerefactor.core.model.ApplyReport;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.service.IssueSelector;
import io.github.krandalf75.rulerefactor.core.service.RefactorEngine;

import java.nio.file.Path;
import java.util.List;

public class AgentApiAdapter {
    private final RefactorEngine engine;

    public AgentApiAdapter(RefactorEngine engine) {
        this.engine = engine;
    }

    public AgentResponse execute(AgentRequest request) {
        if (request == null || request.mode() == null || request.mode().isBlank()) {
            return new AgentResponse("unknown", 0, 0, 0, 0, 0, 0, List.of(), null, "Missing mode");
        }
        String mode = request.mode().trim().toLowerCase();
        Path projectPath = Path.of(request.projectPath() == null || request.projectPath().isBlank() ? "." : request.projectPath());
        String[] rules = request.selection() == null ? null : request.selection().ruleKeys();
        String[] issueIds = request.selection() == null ? null : request.selection().issueIds();

        if ("list-issues".equals(mode)) {
            List<CodeIssue> issues = engine.listIssues(projectPath, rules);
            return new AgentResponse(mode, issues.size(), issues.size(), 0, 0, 0, 0, issues, null, null);
        }

        if ("plan".equals(mode)) {
            List<CodeIssue> detected = engine.listIssues(projectPath, null);
            List<CodeIssue> selected = IssueSelector.filterBySelection(detected, rules, issueIds);
            return new AgentResponse(mode, detected.size(), selected.size(), 0, 0, 0, 0, selected, null, null);
        }

        if ("apply".equals(mode)) {
            ApplyReport report = engine.applySelected(projectPath, rules, issueIds, request.dryRun(), request.verify(), null);
            return new AgentResponse(
                    mode,
                    report.detected(),
                    report.selected(),
                    report.applied(),
                    report.skipped(),
                    report.skippedOutdated(),
                    report.failed(),
                    List.of(),
                    report,
                    null
            );
        }

        return new AgentResponse(mode, 0, 0, 0, 0, 0, 0, List.of(), null, "Unsupported mode: " + mode);
    }
}
