package io.github.krandalf75.rulerefactor.adapters.agent;

import io.github.krandalf75.rulerefactor.core.model.ApplyReport;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;

import java.util.List;

public record AgentResponse(
        String mode,
        int detected,
        int selected,
        int applied,
        int skipped,
        int skippedOutdated,
        int failed,
        List<CodeIssue> issues,
        ApplyReport applyReport,
        String error
) {
}
