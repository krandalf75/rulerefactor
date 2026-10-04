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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class OpenRewriteRecipeFixer implements IssueFixer {
    private final RuleKey ruleKey;
    private final Supplier<Recipe> recipeSupplier;

    public OpenRewriteRecipeFixer(String ruleKey, Supplier<Recipe> recipeSupplier) {
        this.ruleKey = new RuleKey(ruleKey);
        this.recipeSupplier = recipeSupplier;
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

        Recipe recipe = recipeSupplier.get();
        List<Result> results = recipe.run(new InMemoryLargeSourceSet(List.of(sourceFile)), ctx).getChangeset().getAllResults();
        if (results.isEmpty()) {
            return new FixOutcome(false, "No recipe change available");
        }

        Result first = results.getFirst();
        if (first.getAfter() == null) {
            return new FixOutcome(false, "No rewritten source generated");
        }
        String after = first.getAfter().printAll();
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
