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

public class StreamToListDetector implements IssueDetector {
    private static final RuleKey RULE_KEY = new RuleKey("java:S6204");

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
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, Integer p) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, p);
                if (isCollectorsToListUsage(mi)) {
                    String issueId = "ISSUE-" + seq.getAndIncrement();
                    issues.add(new CodeIssue(
                            issueId,
                            RULE_KEY,
                            root.relativize(file).toString(),
                            -1,
                            "Use Stream.toList() instead of Collectors.toList() when possible."
                    ));
                }
                return mi;
            }
        }.visit(cu, 0);
    }

    private boolean isCollectorsToListUsage(J.MethodInvocation mi) {
        if (!"collect".equals(mi.getSimpleName()) || mi.getArguments().size() != 1) {
            return false;
        }
        Expression arg = mi.getArguments().getFirst();
        String argText = arg.toString().replace(" ", "");
        if (argText.contains("Collectors.toList()")) {
            return true;
        }
        if (!(arg instanceof J.MethodInvocation collectorCall)) {
            return false;
        }
        if (!"toList".equals(collectorCall.getSimpleName()) || !collectorCall.getArguments().isEmpty()) {
            return false;
        }
        if (collectorCall.getMethodType() != null && collectorCall.getMethodType().getDeclaringType() instanceof JavaType.FullyQualified fq) {
            if ("java.util.stream.Collectors".equals(fq.getFullyQualifiedName())) {
                return true;
            }
        }
        return isCollectorsReference(collectorCall.getSelect());
    }

    private boolean isCollectorsReference(Expression select) {
        if (select == null) {
            return false;
        }
        if (select instanceof J.Identifier id) {
            return "Collectors".equals(id.getSimpleName());
        }
        if (select instanceof J.FieldAccess fieldAccess) {
            if ("Collectors".equals(fieldAccess.getSimpleName())) {
                return true;
            }
            return isCollectorsReference(fieldAccess.getTarget());
        }
        return false;
    }
}
