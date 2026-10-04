package io.github.krandalf75.rulerefactor.rules.fixers.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.ArrayList;
import java.util.List;

public class RemoveStandardOutputCallsRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Remove System.out/err print calls";
    }

    @Override
    public String getDescription() {
        return "Removes System.out/System.err print/println/printf calls.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.Block visitBlock(J.Block block, ExecutionContext ctx) {
                J.Block b = super.visitBlock(block, ctx);
                List<Statement> updated = new ArrayList<>();
                for (Statement statement : b.getStatements()) {
                    if (statement instanceof J.MethodInvocation mi && isStandardOutputCall(mi)) {
                        maybeRemoveImport("java.io.PrintStream");
                        continue;
                    }
                    updated.add(statement);
                }
                return b.withStatements(updated);
            }

            private boolean isStandardOutputCall(J.MethodInvocation mi) {
                String name = mi.getSimpleName();
                if (!"print".equals(name) && !"println".equals(name) && !"printf".equals(name)) {
                    return false;
                }
                Expression select = mi.getSelect();
                if (!(select instanceof J.FieldAccess fieldAccess)) {
                    return false;
                }
                if (!(fieldAccess.getTarget() instanceof J.Identifier id)) {
                    return false;
                }
                if (!"System".equals(id.getSimpleName())) {
                    return false;
                }
                String stream = fieldAccess.getSimpleName();
                return "out".equals(stream) || "err".equals(stream);
            }
        };
    }
}
