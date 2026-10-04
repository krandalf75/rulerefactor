package io.github.krandalf75.rulerefactor.rules.fixers.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

import java.util.List;

public class UseDiamondOperatorSimpleRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Use diamond operator for new class";
    }

    @Override
    public String getDescription() {
        return "Replaces explicit type arguments in constructor calls with diamond operator where possible.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J.NewClass nc = super.visitNewClass(newClass, ctx);
                if (!(nc.getClazz() instanceof J.ParameterizedType pt)) {
                    return nc;
                }
                if (pt.getTypeParameters() == null || pt.getTypeParameters().isEmpty()) {
                    return nc;
                }
                J.ParameterizedType updated = pt.withTypeParameters(List.of());
                return nc.withClazz(updated);
            }
        };
    }
}
