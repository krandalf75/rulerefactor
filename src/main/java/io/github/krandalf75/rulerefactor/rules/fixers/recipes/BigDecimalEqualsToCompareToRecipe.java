package io.github.krandalf75.rulerefactor.rules.fixers.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

public class BigDecimalEqualsToCompareToRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Replace BigDecimal.equals with compareTo";
    }

    @Override
    public String getDescription() {
        return "Uses compareTo(...) == 0 for BigDecimal value comparison.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);
                if (!"equals".equals(mi.getSimpleName()) || mi.getArguments().size() != 1) {
                    return mi;
                }
                Expression select = mi.getSelect();
                if (select == null || !isBigDecimal(select.getType())) {
                    return mi;
                }

                JavaTemplate template = JavaTemplate.builder("#{any(java.math.BigDecimal)}.compareTo(#{any()}) == 0")
                        .imports("java.math.BigDecimal")
                        .build();
                return template.apply(getCursor(), mi.getCoordinates().replace(), select, mi.getArguments().getFirst());
            }

            private boolean isBigDecimal(JavaType type) {
                if (!(type instanceof JavaType.FullyQualified fq)) {
                    return false;
                }
                return "java.math.BigDecimal".equals(fq.getFullyQualifiedName());
            }
        };
    }
}
