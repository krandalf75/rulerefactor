package io.github.krandalf75.rulerefactor.rules.fixers;

import io.github.krandalf75.rulerefactor.core.api.FixOutcome;
import io.github.krandalf75.rulerefactor.core.api.IssueFixer;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class TextReplaceIssueFixer implements IssueFixer {
    private final RuleKey ruleKey;
    private final Function<String, String> transform;

    public TextReplaceIssueFixer(String ruleKey, Function<String, String> transform) {
        this.ruleKey = new RuleKey(ruleKey);
        this.transform = transform;
    }

    @Override
    public RuleKey ruleKey() {
        return ruleKey;
    }

    @Override
    public boolean supports(CodeIssue issue) {
        return issue != null && ruleKey.value().equals(issue.ruleKey().value());
    }

    @Override
    public FixOutcome apply(CodeIssue issue, Path projectPath, boolean dryRun) {
        Path target = projectPath.resolve(issue.filePath()).normalize();
        String before;
        try {
            before = Files.readString(target);
        } catch (IOException e) {
            return new FixOutcome(false, "Cannot read file: " + issue.filePath());
        }

        String after = transform.apply(before);
        if (after.equals(before)) {
            return new FixOutcome(false, "No matching change pattern found");
        }

        if (!dryRun) {
            try {
                Files.writeString(target, after);
            } catch (IOException e) {
                return new FixOutcome(false, "Cannot write file: " + issue.filePath());
            }
        }
        return new FixOutcome(true, dryRun ? "Dry-run: change previewed" : "Change applied");
    }
}
