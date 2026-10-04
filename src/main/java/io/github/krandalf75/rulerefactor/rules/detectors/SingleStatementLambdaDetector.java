package io.github.krandalf75.rulerefactor.rules.detectors;

import io.github.krandalf75.rulerefactor.core.api.IssueDetector;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.java.Java21Parser;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class SingleStatementLambdaDetector implements IssueDetector {
    private static final RuleKey RULE_KEY = new RuleKey("java:S1602");

    @Override
    public RuleKey ruleKey() {
        return RULE_KEY;
    }

    @Override
    public List<CodeIssue> detect(Path projectPath) {
        List<CodeIssue> issues = new ArrayList();
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
        SourceFile sf;
        try {
            sf = Java21Parser.builder().build().parse(ctx, source).findFirst().orElse(null);
        } catch (Throwable t) {
            throw new IllegalStateException("OpenRewrite parsing failed for " + file, t);
        }
        if (!(sf instanceof J.CompilationUnit cu)) {
            throw new IllegalStateException("OpenRewrite parser did not return a compilation unit for " + file);
        }

        new JavaIsoVisitor<Integer>() {
            @Override
            public J.Lambda visitLambda(J.Lambda lambda, Integer p) {
                J.Lambda l = super.visitLambda(lambda, p);
                if (l.getBody() instanceof J.Block block && block.getStatements().size() == 1) {
                    issues.add(new CodeIssue(
                            "ISSUE-" + seq.getAndIncrement(),
                            RULE_KEY,
                            root.relativize(file).toString(),
                            -1,
                            "Replace this block lambda with a single-expression lambda."
                    ));
                }
                return l;
            }
        }.visit(cu, 0);
    }
}
