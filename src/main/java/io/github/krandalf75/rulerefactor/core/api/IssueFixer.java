package io.github.krandalf75.rulerefactor.core.api;

import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;

import java.nio.file.Path;

public interface IssueFixer {
    RuleKey ruleKey();

    boolean supports(CodeIssue issue);

    FixOutcome apply(CodeIssue issue, Path projectPath, boolean dryRun);
}
