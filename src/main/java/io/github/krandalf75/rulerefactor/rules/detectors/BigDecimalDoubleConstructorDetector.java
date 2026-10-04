package io.github.krandalf75.rulerefactor.rules.detectors;

import io.github.krandalf75.rulerefactor.core.api.IssueDetector;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.java.Java21Parser;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class BigDecimalDoubleConstructorDetector implements IssueDetector {
    private static final RuleKey RULE_KEY = new RuleKey("java:S2111");

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
            public J.NewClass visitNewClass(J.NewClass newClass, Integer p) {
                J.NewClass nc = super.visitNewClass(newClass, p);
                if (isBigDecimalDoubleConstructor(nc)) {
                    issues.add(new CodeIssue(
                            "ISSUE-" + seq.getAndIncrement(),
                            RULE_KEY,
                            root.relativize(file).toString(),
                            -1,
                            "Use BigDecimal.valueOf(...) or String-based constructor instead of BigDecimal(double)."
                    ));
                }
                return nc;
            }
        }.visit(cu, 0);
    }

    private boolean isBigDecimalDoubleConstructor(J.NewClass nc) {
        if (nc.getClazz() == null || nc.getArguments().size() != 1) {
            return false;
        }
        if (!(nc.getClazz() instanceof J.Identifier id) || !"BigDecimal".equals(id.getSimpleName())) {
            return false;
        }
        Expression argument = nc.getArguments().getFirst();
        JavaType argType = argument.getType();
        if (argType instanceof JavaType.Primitive primitive && primitive == JavaType.Primitive.Double) {
            return true;
        }
        if (argument instanceof J.Literal literal) {
            Object value = literal.getValue();
            return value instanceof Double;
        }
        return false;
    }
}
