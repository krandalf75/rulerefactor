package io.github.krandalf75.rulerefactor.rules.fixers;

import io.github.krandalf75.rulerefactor.core.api.FixOutcome;
import io.github.krandalf75.rulerefactor.core.api.IssueFixer;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TryWithResourcesRegexFixer implements IssueFixer {
    private static final Pattern PATTERN = Pattern.compile(
            "(?s)([A-Za-z_][A-Za-z0-9_<>]*)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*new\\s+([A-Za-z_][A-Za-z0-9_<>]*)\\s*\\(([^;]*)\\);\\s*try\\s*\\{([\\s\\S]*?)\\}\\s*finally\\s*\\{\\s*\\2\\.close\\s*\\(\\s*\\)\\s*;\\s*\\}");
    private static final RuleKey RULE_KEY = new RuleKey("java:S2095");

    @Override
    public RuleKey ruleKey() {
        return RULE_KEY;
    }

    @Override
    public boolean supports(CodeIssue issue) {
        return issue != null && RULE_KEY.value().equals(issue.ruleKey().value());
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

        Matcher m = PATTERN.matcher(before);
        if (!m.find()) {
            return new FixOutcome(false, "No try-with-resources candidate found");
        }

        String replacement = "try (" + m.group(1) + " " + m.group(2) + " = new " + m.group(3) + "(" + m.group(4) + ")) {" + m.group(5) + "}";
        String after = m.replaceFirst(Matcher.quoteReplacement(replacement));
        if (after.equals(before)) {
            return new FixOutcome(false, "No source change");
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
