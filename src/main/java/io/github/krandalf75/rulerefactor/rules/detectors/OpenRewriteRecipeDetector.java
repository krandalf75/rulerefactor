package io.github.krandalf75.rulerefactor.rules.detectors;

import io.github.krandalf75.rulerefactor.core.api.IssueDetector;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.java.Java21Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class OpenRewriteRecipeDetector implements IssueDetector {
    private final RuleKey ruleKey;
    private final String message;
    private final Supplier<Recipe> recipeSupplier;

    public OpenRewriteRecipeDetector(String ruleKey, String message, Supplier<Recipe> recipeSupplier) {
        this.ruleKey = new RuleKey(ruleKey);
        this.message = message;
        this.recipeSupplier = recipeSupplier;
    }

    @Override
    public RuleKey ruleKey() {
        return ruleKey;
    }

    @Override
    public List<CodeIssue> detect(Path projectPath) {
        List<CodeIssue> issues = new ArrayList<>();
        AtomicInteger seq = new AtomicInteger(1);

        try (Stream<Path> paths = Files.walk(projectPath)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> scanFile(projectPath, path, issues, seq));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan project path: " + projectPath, e);
        }

        return issues;
    }

    private void scanFile(Path root, Path file, List<CodeIssue> issues, AtomicInteger seq) {
        String source;
        try {
            source = Files.readString(file);
        } catch (IOException e) {
            return;
        }

        InMemoryExecutionContext ctx = new InMemoryExecutionContext(t -> {
        });
        SourceFile sourceFile;
        try {
            sourceFile = Java21Parser.builder().build().parse(ctx, source).findFirst().orElse(null);
        } catch (Throwable t) {
            throw new IllegalStateException("OpenRewrite parsing failed for " + file, t);
        }
        if (sourceFile == null) {
            return;
        }

        Recipe recipe = recipeSupplier.get();
        boolean hasChanges = !recipe.run(new InMemoryLargeSourceSet(List.of(sourceFile)), ctx)
                .getChangeset()
                .getAllResults()
                .isEmpty();

        if (hasChanges) {
            issues.add(new CodeIssue(
                    "ISSUE-" + seq.getAndIncrement(),
                    ruleKey,
                    root.relativize(file).toString(),
                    -1,
                    message
            ));
        }
    }
}
