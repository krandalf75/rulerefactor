package io.github.krandalf75.rulerefactor.core.api;

import io.github.krandalf75.rulerefactor.core.model.RuleKey;

public record RuleCapability(RuleKey ruleKey, boolean detectable, boolean fixable, String confidence) {
}
