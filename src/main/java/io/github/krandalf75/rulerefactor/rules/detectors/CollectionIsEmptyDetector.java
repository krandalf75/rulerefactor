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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class CollectionIsEmptyDetector implements IssueDetector {
    private static final RuleKey RULE_KEY = new RuleKey("java:S1155");

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
            public J.Binary visitBinary(J.Binary binary, Integer p) {
                J.Binary b = super.visitBinary(binary, p);

                if (isSizeCallComparedToZero(b.getLeft(), b.getOperator(), b.getRight())
                        || isSizeCallComparedToZero(b.getRight(), b.getOperator(), b.getLeft())) {
                    String issueId = "ISSUE-" + seq.getAndIncrement();
                    issues.add(new CodeIssue(
                            issueId,
                            RULE_KEY,
                            root.relativize(file).toString(),
                            -1,
                            "Use isEmpty() to check if the collection is empty."
                    ));
                }

                return b;
            }
        }.visit(cu, 0);
    }

    private boolean isSizeCallComparedToZero(Expression sizeSide, J.Binary.Type op, Expression zeroSide) {
        if (!isZeroLiteral(unwrap(zeroSide))) {
            return false;
        }
        if (!isSizeCall(unwrap(sizeSide))) {
            return false;
        }
        return op == J.Binary.Type.Equal || op == J.Binary.Type.NotEqual
                || op == J.Binary.Type.LessThan || op == J.Binary.Type.GreaterThan
                || op == J.Binary.Type.LessThanOrEqual || op == J.Binary.Type.GreaterThanOrEqual;
    }

    private boolean isZeroLiteral(Expression expr) {
        if (!(expr instanceof J.Literal literal)) {
            return false;
        }
        Object value = literal.getValue();
        if (value instanceof Number) {
            return ((Number) value).doubleValue() == 0d;
        }
        String source = literal.getValueSource();
        if (source == null) {
            return false;
        }
        String normalized = source.replace("_", "").trim();
        return "0".equals(normalized)
                || "0L".equals(normalized)
                || "0l".equals(normalized)
                || "0.0".equals(normalized)
                || "0.0d".equalsIgnoreCase(normalized)
                || "0.0f".equalsIgnoreCase(normalized);
    }

    private Expression unwrap(Expression expression) {
        if (expression instanceof J.Parentheses<?> parentheses && parentheses.getTree() instanceof Expression inner) {
            return inner;
        }
        return expression;
    }

    private boolean isSizeCall(Expression expression) {
        if (expression instanceof J.MethodInvocation mi) {
            return "size".equals(mi.getSimpleName());
        }
        String printed = expression.toString().replace(" ", "");
        return printed.endsWith(".size()");
    }
}
