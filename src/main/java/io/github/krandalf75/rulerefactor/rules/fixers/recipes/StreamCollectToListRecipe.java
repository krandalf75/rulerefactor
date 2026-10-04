package io.github.krandalf75.rulerefactor.rules.fixers.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class StreamCollectToListRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "Replace collect(Collectors.toList()) with toList()";
    }

    @Override
    public String getDescription() {
        return "Uses Stream.toList() instead of collect(Collectors.toList()) where pattern matches.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);
                if (!"collect".equals(mi.getSimpleName()) || mi.getArguments().size() != 1) {
                    return mi;
                }
                Expression arg = mi.getArguments().getFirst();
                if (!(arg instanceof J.MethodInvocation collectorCall)) {
                    return mi;
                }
                if (!"toList".equals(collectorCall.getSimpleName())) {
                    return mi;
                }
                if (collectorCall.getSelect() == null || !collectorCall.getSelect().toString().contains("Collectors")) {
                    return mi;
                }
                JavaTemplate template = JavaTemplate.builder("#{any(java.util.stream.Stream)}.toList()")
                        .imports("java.util.stream.Stream")
                        .build();
                return template.apply(getCursor(), mi.getCoordinates().replace(), mi.getSelect());
            }
        };
    }
}
