package io.github.krandalf75.rulerefactor.rules.fixers;

import io.github.krandalf75.rulerefactor.core.api.FixOutcome;
import io.github.krandalf75.rulerefactor.core.api.IssueFixer;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.java.Java21Parser;
import org.openrewrite.java.RemoveUnusedImports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class UnusedImportFixer implements IssueFixer {
    private static final RuleKey RULE_KEY = new RuleKey("java:S1128");

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

        Recipe recipe = new RemoveUnusedImports();
        InMemoryExecutionContext ctx = new InMemoryExecutionContext(t -> {
        });
        SourceFile sourceFile;
        try {
            sourceFile = Java21Parser.builder().build().parse(ctx, before).findFirst().orElse(null);
        } catch (Throwable t) {
            return new FixOutcome(false, "OpenRewrite parse failed: " + t.getMessage());
        }
        if (sourceFile == null) {
            return new FixOutcome(false, "No source parsed");
        }

        List<Result> results = recipe.run(new InMemoryLargeSourceSet(List.of(sourceFile)), ctx).getChangeset().getAllResults();
        if (results.isEmpty()) {
            return new FixOutcome(false, "No unused import change available");
        }

        String after = results.getFirst().getAfter() == null ? before : results.getFirst().getAfter().printAll();
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
        return new FixOutcome(true, dryRun ? "Dry-run: unused imports removable" : "Unused imports removed");
    }
}
