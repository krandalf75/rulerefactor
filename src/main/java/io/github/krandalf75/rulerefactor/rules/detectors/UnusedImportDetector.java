package io.github.krandalf75.rulerefactor.rules.detectors;

import io.github.krandalf75.rulerefactor.core.api.IssueDetector;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class UnusedImportDetector implements IssueDetector {
    private static final RuleKey RULE_KEY = new RuleKey("java:S1128");

    @Override
    public RuleKey ruleKey() {
        return RULE_KEY;
    }

    @Override
    public List<CodeIssue> detect(Path projectPath) {
        List<CodeIssue> issues = new ArrayList();
        AtomicInteger seq = new AtomicInteger(1);

        Recipe recipe = new RemoveUnusedImports();
        InMemoryExecutionContext ctx = new InMemoryExecutionContext(throwable -> {
        });

        try (Stream<Path> paths = Files.walk(projectPath)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> detectInFile(recipe, ctx, projectPath, path, issues, seq));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan project path: " + projectPath, e);
        }

        return issues;
    }

    private void detectInFile(
            Recipe recipe,
            InMemoryExecutionContext ctx,
            Path root,
            Path javaFile,
            List<CodeIssue> issues,
            AtomicInteger seq
    ) {
        String before;
        try {
            before = Files.readString(javaFile);
        } catch (IOException e) {
            return;
        }

        SourceFile sourceFile;

        try {
            sourceFile = Java21Parser.builder()
                    .build()
                    .parse(ctx, before)
                    .findFirst()
                    .orElse(null);
        } catch (Throwable t) {
            throw new IllegalStateException("OpenRewrite parsing failed for " + javaFile, t);
        }

        if (sourceFile == null) {
            return;
        }

        List<Result> results = recipe.run(new InMemoryLargeSourceSet(List.of(sourceFile)), ctx).getChangeset().getAllResults();
        if (results.isEmpty()) {
            return;
        }

        for (Result result : results) {
            SourceFile beforeFile = result.getBefore();
            SourceFile afterFile = result.getAfter();
            if (beforeFile == null || afterFile == null) {
                continue;
            }

            Set<String> beforeImports = importLines(beforeFile.printAll());
            Set<String> afterImports = importLines(afterFile.printAll());

            beforeImports.removeAll(afterImports);
            for (String removedImport : beforeImports) {
                int line = findLine(before, removedImport);
                String issueId = "ISSUE-" + seq.getAndIncrement();
                String relativePath = root.relativize(javaFile).toString();
                String message = "Remove this unused import: " + removedImport.replace("import", "").replace(";", "").trim();
                issues.add(new CodeIssue(issueId, RULE_KEY, relativePath, line, message));
            }
        }
    }

    private Set<String> importLines(String source) {
        Set<String> imports = new HashSet();
        String[] lines = source.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("import ")) {
                imports.add(trimmed);
            }
        }
        return imports;
    }

    private int findLine(String source, String exactLine) {
        String[] lines = source.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().equals(exactLine)) {
                return i + 1;
            }
        }
        return -1;
    }
}
