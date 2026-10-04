package io.github.krandalf75.rulerefactor.core.api;

import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;

import java.nio.file.Path;
import java.util.List;

public interface IssueDetector {
    RuleKey ruleKey();

    List<CodeIssue> detect(Path projectPath);
}
