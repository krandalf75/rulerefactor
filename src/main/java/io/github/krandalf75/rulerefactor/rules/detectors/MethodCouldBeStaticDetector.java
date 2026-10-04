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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class MethodCouldBeStaticDetector implements IssueDetector {
    private static final RuleKey RULE_KEY = new RuleKey("java:S2325");

    @Override
    public RuleKey ruleKey() {
        return RULE_KEY;
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
        InMemoryExecutionContext ctx = new InMemoryExecutionContext(t -> {});
        SourceFile sf = Java21Parser.builder().build().parse(ctx, source).findFirst().orElse(null);
        if (!(sf instanceof J.CompilationUnit cu)) {
            return;
        }

        new JavaIsoVisitor<Integer>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, Integer p) {
                J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, p);
                if (cd.getBody() == null) {
                    return cd;
                }
                Set<String> fieldNames = new HashSet<>();
                for (J statement : cd.getBody().getStatements()) {
                    if (statement instanceof J.VariableDeclarations fields) {
                        for (J.VariableDeclarations.NamedVariable variable : fields.getVariables()) {
                            fieldNames.add(variable.getSimpleName());
                        }
                    }
                }

                for (J statement : cd.getBody().getStatements()) {
                    if (!(statement instanceof J.MethodDeclaration method)) {
                        continue;
                    }
                    if (method.getBody() == null || method.getSimpleName().equals("<init>")) {
                        continue;
                    }
                    boolean isPrivate = method.getModifiers().stream().anyMatch(m -> m.getType() == J.Modifier.Type.Private);
                    boolean isStatic = method.getModifiers().stream().anyMatch(m -> m.getType() == J.Modifier.Type.Static);
                    if (!isPrivate || isStatic) {
                        continue;
                    }
                    String body = method.getBody().toString();
                    if (body.contains("this.") || body.contains("super.")) {
                        continue;
                    }
                    boolean usesField = fieldNames.stream().anyMatch(body::contains);
                    if (usesField) {
                        continue;
                    }
                    issues.add(new CodeIssue(
                            "ISSUE-" + seq.getAndIncrement(),
                            RULE_KEY,
                            root.relativize(file).toString(),
                            -1,
                            "Make this method static."
                    ));
                }
                return cd;
            }
        }.visit(cu, 0);
    }
}
