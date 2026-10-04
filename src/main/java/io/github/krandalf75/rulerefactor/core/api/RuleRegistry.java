package io.github.krandalf75.rulerefactor.core.api;

import io.github.krandalf75.rulerefactor.core.model.RuleKey;

import java.util.List;
import java.util.Optional;

public interface RuleRegistry {
    Optional<IssueDetector> detector(RuleKey key);

    Optional<IssueFixer> fixer(RuleKey key);

    List<RuleCapability> capabilities();
}
