package io.github.krandalf75.rulerefactor.rules.detectors;

import io.github.krandalf75.rulerefactor.core.api.IssueDetector;
import io.github.krandalf75.rulerefactor.core.model.CodeIssue;
import io.github.krandalf75.rulerefactor.core.model.RuleKey;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.java.Java21Parser;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class UnusedLocalVariableDetector implements IssueDetector {
    private static final RuleKey RULE_KEY = new RuleKey("java:S1481");

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
            public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, Integer p) {
                J.VariableDeclarations vars = super.visitVariableDeclarations(multiVariable, p);
                if (!(getCursor().getParentTreeCursor().getValue() instanceof J.Block block)) {
                    return vars;
                }
                Object blockParent = getCursor().getParentTreeCursor().getParentTreeCursor().getValue();
                if (!(blockParent instanceof J.MethodDeclaration)
                        && !(blockParent instanceof J.ForLoop)
                        && !(blockParent instanceof J.ForEachLoop)
                        && !(blockParent instanceof J.WhileLoop)
                        && !(blockParent instanceof J.DoWhileLoop)
                        && !(blockParent instanceof J.If)
                        && !(blockParent instanceof J.Try)
                        && !(blockParent instanceof J.Case)
                        && !(blockParent instanceof J.Lambda)) {
                    return vars;
                }
                if (!(vars instanceof Statement declarationStatement)) {
                    return vars;
                }

                int declarationIndex = block.getStatements().indexOf(declarationStatement);
                if (declarationIndex < 0) {
                    return vars;
                }

                for (J.VariableDeclarations.NamedVariable variable : vars.getVariables()) {
                    String name = variable.getSimpleName();
                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    if (!isUsedAfterDeclaration(block, declarationIndex, name)) {
                        issues.add(new CodeIssue(
                                "ISSUE-" + seq.getAndIncrement(),
                                RULE_KEY,
                                root.relativize(file).toString(),
                                -1,
                                "Remove this unused local variable."
                        ));
                    }
                }

                return vars;
            }
        }.visit(cu, 0);
    }

    private boolean isUsedAfterDeclaration(J.Block block, int declarationIndex, String variableName) {
        for (int i = declarationIndex + 1; i < block.getStatements().size(); i++) {
            if (containsIdentifierReference(block.getStatements().get(i), variableName)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsIdentifierReference(J tree, String variableName) {
        AtomicBoolean found = new AtomicBoolean(false);
        new JavaIsoVisitor<Integer>() {
            @Override
            public J.Identifier visitIdentifier(J.Identifier identifier, Integer p) {
                J.Identifier id = super.visitIdentifier(identifier, p);
                if (variableName.equals(id.getSimpleName())) {
                    found.set(true);
                }
                return id;
            }
        }.visit(tree, 0);
        return found.get();
    }
}
